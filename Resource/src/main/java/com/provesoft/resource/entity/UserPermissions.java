package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity is a copy of the RolePermission, applied to a specific User.
 * The copy allows fine-grain permission editting for a user.
 */
@Entity
public class UserPermissions {

    public UserPermissions(Long userId,
                           Long organizationId,
                           Boolean viewPerm,
                           Boolean revisePerm,
                           Boolean commentPerm,
                           Boolean adminPerm)
    {
        this.key = new UserPermissionsKey(userId, organizationId);
        this.viewPerm = viewPerm;
        this.revisePerm = revisePerm;
        this.commentPerm = commentPerm;
        this.adminPerm = adminPerm;
    }

    public UserPermissions() {
        // Default constructor
    }

    @EmbeddedId
    private UserPermissionsKey key;

    private Boolean viewPerm;
    private Boolean revisePerm;
    private Boolean commentPerm;
    private Boolean adminPerm;

    public UserPermissionsKey getKey() {
        return key;
    }

    public void setKey(UserPermissionsKey key) {
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
