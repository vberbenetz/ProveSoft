package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RoleUserKey implements Serializable {

    public RoleUserKey (String companyName, Long userId, Long roleId) {
        this.companyName = companyName;
        this.userId = userId;
        this.roleId = roleId;
    }

    public RoleUserKey() {
        // Public constructor
    }

    private String companyName;
    private Long userId;
    private Long roleId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
