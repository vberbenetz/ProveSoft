package com.provesoft.resource.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SystemSettings {

    private SystemSettings (String setting, String value) {
        this.setting = setting;
        this.value = value;
    }

    @Id
    private String setting;
    private String value;

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
