package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.MyPlateListsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/plates")
public interface IUserPlateListController {

    @GetMapping("/my-lists")
    ResponseEntity<DataResult<MyPlateListsDto>> getMyLists(
            @RequestAttribute("userId") Long currentUserId);

    @PostMapping("/{plateCode}/save")
    ResponseEntity<Result> savePlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);

    @DeleteMapping("/{plateCode}/save")
    ResponseEntity<Result> unsavePlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);

    @PostMapping("/{plateCode}/alarm")
    ResponseEntity<Result> createAlarm(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);

    @DeleteMapping("/{plateCode}/alarm")
    ResponseEntity<Result> removeAlarm(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);
}
