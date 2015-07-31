package com.provesoft.gateway.service;

import com.provesoft.gateway.entity.SystemSettings;
import com.provesoft.gateway.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

    @Autowired
    SystemSettingsRepository systemSettingsRepository;

    public SystemSettings saveSetting (SystemSettings systemSetting) {
        return systemSettingsRepository.saveAndFlush(systemSetting);
    }
}
