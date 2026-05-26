package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateRemovalRequestService;
import com.mefy.platemate.business.utilities.constants.Messages;
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
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
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
        Plate plate = plateDao.findById(plateId).orElse(null);
        if (plate == null) {
            return new ErrorDataResult<>(messageService.getMessage("plate.removal.plate.not.found"));
        }
        if (userDao.findByIdAndActiveTrue(requesterUserId).isEmpty()) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        LocalDateTime now = LocalDateTime.now();
        PlateRemovalRequest removalRequest = new PlateRemovalRequest();
        removalRequest.setPlate(plate);
        removalRequest.setRequesterUserId(requesterUserId);
        removalRequest.setRequesterEmail(request.getRequesterEmail() == null ? null : request.getRequesterEmail().trim());
        removalRequest.setReason(request.getReason());
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

        return new SuccessDataResult<>(toDto(saved), messageService.getMessage("plate.removal.request.created"));
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
                messageService.getMessage("plate.removal.requests.listed")
        );
    }

    @Override
    @Transactional
    public Result reviewRequest(Long requestId, Long reviewerUserId, ReviewPlateRemovalRequestRequest request) {
        PlateRemovalRequest removalRequest = plateRemovalRequestDao.findById(requestId).orElse(null);
        if (removalRequest == null) {
            return new ErrorResult(messageService.getMessage("plate.removal.request.not.found"));
        }

        Plate plate = removalRequest.getPlate();
        LocalDateTime now = LocalDateTime.now();
        removalRequest.setStatus(request.getStatus());
        removalRequest.setAdminNote(request.getAdminNote() == null ? null : request.getAdminNote().trim());
        removalRequest.setReviewedBy(reviewerUserId);
        removalRequest.setReviewedAt(now);
        removalRequest.setUpdatedAt(now);
        plateRemovalRequestDao.save(removalRequest);

        if (plate != null) {
            if (request.getStatus() == PlateRemovalRequestStatus.ACCEPTED) {
                plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);
                if (request.getAdminNote() != null && !request.getAdminNote().isBlank()) {
                    plate.setHiddenReason(request.getAdminNote().trim());
                } else {
                    plate.setHiddenReason("HIDDEN_BY_ACCEPTED_REMOVAL_REQUEST:" + requestId);
                }
                plate.setUpdatedAt(now);
                plateDao.save(plate);
            } else if (request.getStatus() == PlateRemovalRequestStatus.REJECTED
                    && plate.getStatus() == PlateStatus.HIDDEN_BY_REQUEST
                    && plate.getHiddenReason() != null
                    && plate.getHiddenReason().startsWith(AUTO_HIDE_MARKER + ":")) {
                plate.setStatus(PlateStatus.ACTIVE);
                plate.setHiddenReason(null);
                plate.setUpdatedAt(now);
                plateDao.save(plate);
            }
        }

        return new SuccessResult(messageService.getMessage("plate.removal.request.reviewed"));
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
                request.getReason(),
                request.getDescription(),
                request.getStatus(),
                request.getAdminNote(),
                request.getCreatedAt(),
                request.getReviewedAt(),
                request.getReviewedBy()
        );
    }
}
