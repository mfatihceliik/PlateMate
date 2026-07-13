package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.AdminMenuItemDto;

import java.util.List;

public interface IAdminMenuService {
    DataResult<List<AdminMenuItemDto>> getMenu();
}
