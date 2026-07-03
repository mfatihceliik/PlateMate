package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserPlateListController;
import com.mefy.platemate.business.abstracts.IUserPlateListService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.MyPlateListsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserPlateListController implements IUserPlateListController {

    private final IUserPlateListService userPlateListService;

    @Override
    public ResponseEntity<DataResult<MyPlateListsDto>> getMyLists(
            @RequestAttribute("userId") Long currentUserId) {
        DataResult<MyPlateListsDto> result = userPlateListService.getMyLists(currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> savePlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userPlateListService.savePlate(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> unsavePlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userPlateListService.unsavePlate(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> createAlarm(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userPlateListService.createAlarm(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> removeAlarm(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userPlateListService.removeAlarm(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
