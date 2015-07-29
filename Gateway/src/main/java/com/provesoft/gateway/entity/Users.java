package com.provesoft.gateway.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Used by the GATEWAY to authenticate users
 */
@Entity
public class Users {

    public Users (String username, String password, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public Users() {
        // Default constructor
    }

    @Id
    private String username;
    private String password;
    private Boolean enabled;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
