package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class OrgUser {

    public OrgUser (Long orgId, Long userId, String companyName) {

        this.key = new OrgUserKey(orgId, userId, companyName);
    }

    public OrgUser() {
        // Public constructor
    }

    @EmbeddedId
    private OrgUserKey key;

    public OrgUserKey getKey() {
        return key;
    }

    public void setKey(OrgUserKey key) {
        this.key = key;
    }
}
