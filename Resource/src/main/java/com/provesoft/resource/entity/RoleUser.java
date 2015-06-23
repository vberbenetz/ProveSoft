package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class RoleUser {

    public RoleUser (Long roleId, Long userId, String companyName) {
        this.key = new RoleUserKey(roleId, userId, companyName);
    }

    public RoleUser() {
        // Public constructor
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
