package com.provesoft.resource.service;

import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

    @Autowired
    SystemSettingsRepository systemSettingsRepository;

    public SystemSettings getSettingByCompanyNameAndSetting(String companyName, String setting) {
        return systemSettingsRepository.findByKeyCompanyNameAndKeySetting(companyName, setting);
    }

    public SystemSettings saveSetting (SystemSettings systemSetting) {
        return systemSettingsRepository.saveAndFlush(systemSetting);
    }
}
