package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity holds organization permissions which are applied to a specific role.
 * Contains "view", "revise", "comment", and "admin" permissions.
 */
@Entity
public class RolePermissions {

    public RolePermissions(Long roleId,
                           Long organizationId,
                           Boolean viewPerm,
                           Boolean revisePerm,
                           Boolean commentPerm,
                           Boolean adminPerm)
    {
        this.key = new RolePermissionsKey(roleId, organizationId);
        this.viewPerm = viewPerm;
        this.revisePerm = revisePerm;
        this.commentPerm = commentPerm;
        this.adminPerm = adminPerm;
    }

    public RolePermissions() {
        // Default constructor
    }

    @EmbeddedId
    private RolePermissionsKey key;

    private Boolean viewPerm;
    private Boolean revisePerm;
    private Boolean commentPerm;
    private Boolean adminPerm;

    public RolePermissionsKey getKey() {
        return key;
    }

    public void setKey(RolePermissionsKey key) {
        this.key = key;
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
