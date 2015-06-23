package com.provesoft.resource.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RoleUserKey implements Serializable {

    public RoleUserKey (Long roleId, Long userId, String companyName) {
        this.roleId = roleId;
        this.userId = userId;
        this.companyName = companyName;
    }

    public RoleUserKey() {
        // Public constructor
    }

    @Column(name = "roleId", nullable = false)
    private Long roleId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "companyName", nullable = false)
    private String companyName;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
