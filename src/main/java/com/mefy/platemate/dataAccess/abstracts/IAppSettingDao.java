package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAppSettingDao extends JpaRepository<AppSetting, String> {
}
