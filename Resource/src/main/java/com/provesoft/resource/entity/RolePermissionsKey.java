package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RolePermissionsKey implements Serializable {

    public RolePermissionsKey (Long roleId, Long organizationId) {
        this.roleId = roleId;
        this.organizationId = organizationId;
    }

    public RolePermissionsKey() {
        // Default constructor
    }

    private Long roleId;
    private Long organizationId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
