package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


@Entity
public class SystemSettings {

    private SystemSettings (String company, String setting, String value) {
        this.key = new SystemSettingsKey(company, setting);
        this.value = value;
    }

    public SystemSettings() {
        // Public Constructor
    }

    @EmbeddedId
    private SystemSettingsKey key;

    private String value;

    public SystemSettingsKey getKey() {
        return key;
    }

    public void setKey(SystemSettingsKey key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
