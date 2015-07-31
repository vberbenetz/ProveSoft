package com.provesoft.gateway.repository;

import com.provesoft.gateway.entity.SystemSettings;
import com.provesoft.gateway.entity.SystemSettingsKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, SystemSettingsKey> {

    SystemSettings findByKeyCompanyNameAndKeySetting(String companyName, String setting);
}
