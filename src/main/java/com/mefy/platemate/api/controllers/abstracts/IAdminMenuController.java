package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.AdminMenuItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/menu")
public interface IAdminMenuController {

    @GetMapping
    ResponseEntity<DataResult<List<AdminMenuItemDto>>> getMenu(
            @RequestAttribute("userId") Long currentUserId
    );
}
