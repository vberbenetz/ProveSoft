package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity maps a User to Role.
 */
@Entity
public class RoleUser {

    public RoleUser (String companyName, Long userId, Long roleId) {
        this.key = new RoleUserKey(companyName, userId, roleId);
    }

    public RoleUser() {
        // Default Constructor
    }

    @EmbeddedId
    private RoleUserKey key;

    public RoleUserKey getKey() {
        return key;
    }

    public void setKey(RoleUserKey key) {
        this.key = key;
    }
}
