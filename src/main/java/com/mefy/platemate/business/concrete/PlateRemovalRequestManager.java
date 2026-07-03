package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateRemovalRequestService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateRemovalRequest;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestRequest;
import com.mefy.platemate.entities.dto.request.ReviewPlateRemovalRequestRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlateRemovalRequestManager implements IPlateRemovalRequestService {

    private static final String AUTO_HIDE_MARKER = "AUTO_HIDE_BY_REMOVAL_REQUEST";

    private final IPlateRemovalRequestDao plateRemovalRequestDao;
    private final IPlateDao plateDao;
    private final IUserDao userDao;
    private final IMessageService messageService;

    @Value("${moderation.hide-plate-on-removal-request:true}")
    private boolean hidePlateOnRemovalRequest = true;

    @Override
    @Transactional
    public DataResult<PlateRemovalRequestDto> addRequest(
            Long plateId,
            Long requesterUserId,
            AddPlateRemovalRequestRequest request
    ) {
        PlateRemovalRequestReason reason = request == null
                ? null
                : PlateRemovalRequestReason.resolve(request.getReasonId(), request.getReasonCode());
        Plate plate = plateDao.findById(plateId).orElse(null);

        Result validationResult = BusinessRules.run(
                checkIfReasonExists(reason),
                checkIfPlateExists(plate),
                checkIfUserExists(requesterUserId)
        );
        if (validationResult != null) {
            return new ErrorDataResult<>(validationResult.getMessage());
        }

        User requester = userDao.findByIdAndActiveTrue(requesterUserId).orElse(null);

        LocalDateTime now = LocalDateTime.now();
        PlateRemovalRequest removalRequest = new PlateRemovalRequest();
        removalRequest.setPlate(plate);
        removalRequest.setRequesterUserId(requesterUserId);
        removalRequest.setRequesterUsername(requester == null ? null : requester.getUsername());
        String requesterEmail = (request.getRequesterEmail() == null || request.getRequesterEmail().isBlank())
                ? (requester == null ? null : requester.getEmail())
                : request.getRequesterEmail().trim();
        removalRequest.setRequesterEmail(requesterEmail);
        removalRequest.setReason(reason);
        removalRequest.setDescription(request.getDescription().trim());
        removalRequest.setStatus(PlateRemovalRequestStatus.OPEN);
        removalRequest.setCreatedAt(now);
        removalRequest.setUpdatedAt(now);
        PlateRemovalRequest saved = plateRemovalRequestDao.save(removalRequest);

        if (hidePlateOnRemovalRequest && plate.getStatus() == PlateStatus.ACTIVE) {
            plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);
            plate.setHiddenReason(AUTO_HIDE_MARKER + ":" + saved.getId());
            plate.setUpdatedAt(now);
            plateDao.save(plate);
        }

        return new SuccessDataResult<>(toDto(saved), messageService.getMessage(Messages.PLATE_REMOVAL_REQUEST_CREATED));
    }

    private Result checkIfReasonExists(PlateRemovalRequestReason reason) {
        if (reason == null) {
            return new ErrorResult(messageService.getMessage(Messages.VALIDATION_PLATE_REMOVAL_REASON_NOTNULL));
        }
        return new SuccessResult();
    }

    private Result checkIfPlateExists(Plate plate) {
        if (plate == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_REMOVAL_PLATE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfUserExists(Long userId) {
        if (userDao.findByIdAndActiveTrue(userId).isEmpty()) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    @Override
    public DataResult<PagedData<PlateRemovalRequestDto>> getRequests(PaginationRequest paginationRequest) {
        var pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = plateRemovalRequestDao.findAll(pageable).map(this::toDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage(Messages.PLATE_REMOVAL_REQUESTS_LISTED)
        );
    }

    @Override
    @Transactional
    public Result reviewRequest(Long requestId, Long reviewerUserId, ReviewPlateRemovalRequestRequest request) {
        PlateRemovalRequest removalRequest = plateRemovalRequestDao.findById(requestId).orElse(null);
        if (removalRequest == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_REMOVAL_REQUEST_NOT_FOUND));
        }
        PlateRemovalRequestStatus nextStatus = request == null
                ? null
                : PlateRemovalRequestStatus.resolve(request.getStatusId(), request.getStatusCode());
        if (nextStatus == null) {
            return new ErrorResult(messageService.getMessage(Messages.VALIDATION_PLATE_REMOVAL_STATUS_NOTNULL));
        }

        Plate plate = removalRequest.getPlate();
        LocalDateTime now = LocalDateTime.now();
        removalRequest.setStatus(nextStatus);
        removalRequest.setAdminNote(request.getAdminNote() == null ? null : request.getAdminNote().trim());
        removalRequest.setReviewedBy(reviewerUserId);
        removalRequest.setReviewedAt(now);
        removalRequest.setUpdatedAt(now);
        plateRemovalRequestDao.save(removalRequest);

        if (plate != null) {
            if (nextStatus == PlateRemovalRequestStatus.ACCEPTED) {
                plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);
                if (request.getAdminNote() != null && !request.getAdminNote().isBlank()) {
                    plate.setHiddenReason(request.getAdminNote().trim());
                } else {
                    plate.setHiddenReason("HIDDEN_BY_ACCEPTED_REMOVAL_REQUEST:" + requestId);
                }
                plate.setUpdatedAt(now);
                plateDao.save(plate);
            } else if (nextStatus == PlateRemovalRequestStatus.REJECTED
                    && plate.getStatus() == PlateStatus.HIDDEN_BY_REQUEST
                    && plate.getHiddenReason() != null
                    && plate.getHiddenReason().startsWith(AUTO_HIDE_MARKER + ":")) {
                plate.setStatus(PlateStatus.ACTIVE);
                plate.setHiddenReason(null);
                plate.setUpdatedAt(now);
                plateDao.save(plate);
            }
        }

        return new SuccessResult(messageService.getMessage(Messages.PLATE_REMOVAL_REQUEST_REVIEWED));
    }

    private PlateRemovalRequestDto toDto(PlateRemovalRequest request) {
        String plateCode = request.getPlate() != null ? request.getPlate().getPlateCode() : null;
        Long plateId = request.getPlate() != null ? request.getPlate().getId() : null;
        return new PlateRemovalRequestDto(
                request.getId(),
                plateId,
                plateCode,
                request.getRequesterUserId(),
                request.getRequesterEmail(),
                request.getRequesterUsername(),
                request.getReasonId(),
                request.getReasonCode(),
                request.getDescription(),
                request.getStatusId(),
                request.getStatusCode(),
                request.getAdminNote(),
                request.getCreatedAt(),
                request.getReviewedAt(),
                request.getReviewedBy()
        );
    }
}
