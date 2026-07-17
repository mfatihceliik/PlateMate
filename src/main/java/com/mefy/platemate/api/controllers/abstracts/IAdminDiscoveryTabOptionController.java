package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/discovery-tab-options")
public interface IAdminDiscoveryTabOptionController {

    @GetMapping
    ResponseEntity<DataResult<List<DiscoveryTabOptionAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping
    ResponseEntity<DataResult<DiscoveryTabOptionAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddDiscoveryTabOptionRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<DataResult<DiscoveryTabOptionAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateDiscoveryTabOptionRequest request
    );

    @PatchMapping("/{id}/active")
    ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateDiscoveryTabOptionActiveRequest request
    );
}
