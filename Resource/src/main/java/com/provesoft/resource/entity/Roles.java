package com.provesoft.resource.entity;


import javax.persistence.*;

@Entity
public class Roles {

    public Roles (
            String name,
            String companyName,
            Long organizationId,
            Boolean viewPerm,
            Boolean revisePerm,
            Boolean commentPerm,
            Boolean adminPerm) {

        this.name = name;
        this.companyName = companyName;
        this.organizationId = organizationId;
        this.viewPerm = viewPerm;
        this.revisePerm = revisePerm;
        this.commentPerm = commentPerm;
        this.adminPerm = adminPerm;
    }

    public Roles() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long roleId;

    private String companyName;
    private String name;
    private Long organizationId;
    private Boolean viewPerm;
    private Boolean revisePerm;
    private Boolean commentPerm;
    private Boolean adminPerm;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getViewPerm() {
        return viewPerm;
    }

    public void setViewPerm(Boolean viewPerm) {
        this.viewPerm = viewPerm;
    }

    public Boolean getRevisePerm() {
        return revisePerm;
    }

    public void setRevisePerm(Boolean revisePerm) {
        this.revisePerm = revisePerm;
    }

    public Boolean getCommentPerm() {
        return commentPerm;
    }

    public void setCommentPerm(Boolean commentPerm) {
        this.commentPerm = commentPerm;
    }

    public Boolean getAdminPerm() {
        return adminPerm;
    }

    public void setAdminPerm(Boolean adminPerm) {
        this.adminPerm = adminPerm;
    }

}
