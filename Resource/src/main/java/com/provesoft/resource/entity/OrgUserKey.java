package com.provesoft.resource.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class OrgUserKey implements Serializable {

    public OrgUserKey (Long organizationId, Long userId, String companyName) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.companyName = companyName;
    }

    public OrgUserKey() {
        // Public constructor
    }

    @Column(name = "organizationId", nullable = false)
    private Long organizationId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "companyName", nullable = false)
    private String companyName;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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
