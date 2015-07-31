package com.provesoft.gateway.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class SystemSettingsKey implements Serializable {

    public SystemSettingsKey (String companyName, String setting) {
        this.setting = setting;
        this.companyName = companyName;
    }

    public SystemSettingsKey() {
        // Public Constructor
    }

    private String setting;
    private String companyName;

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
