package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.entity.SystemSettingsKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, SystemSettingsKey> {

    SystemSettings findByKeyCompanyNameAndKeySetting(String companyName, String setting);
}
