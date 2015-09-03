package com.provesoft.resource.controller;

import com.provesoft.resource.entity.SystemSettings;
import com.provesoft.resource.service.SystemSettingsService;
import com.provesoft.resource.utils.UserHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
public class SystemSettingsController {

    @Autowired
    SystemSettingsService systemSettingsService;

    /* -------------------------------------------------------- */
    /* ------------------------ GET --------------------------- */
    /* -------------------------------------------------------- */

    /**
     * Method retrieves SystemSettings by setting name
     * @param setting Setting name
     * @param auth Authentication object
     * @return SystemSettings
     */
    @RequestMapping(
            value = "/setting",
            method = RequestMethod.GET
    )
    public SystemSettings findSystemSetting(@RequestParam("setting") String setting,
                                            Authentication auth) {

        String companyName = UserHelpers.getCompany(auth);

        return systemSettingsService.getSettingByCompanyNameAndSetting(companyName, setting);
    }

}
