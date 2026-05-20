package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;

public interface IAdminAccessService {
    Result checkAdmin(Long userId);
}
