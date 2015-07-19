package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {
}
