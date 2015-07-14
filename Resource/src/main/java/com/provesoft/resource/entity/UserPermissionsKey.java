package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserPermissionsKey implements Serializable {

    public UserPermissionsKey(Long userId, Long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    public UserPermissionsKey() {
        // Default constructor
    }

    private Long userId;
    private Long organizationId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
