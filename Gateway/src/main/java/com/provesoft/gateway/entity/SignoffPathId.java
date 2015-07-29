package com.provesoft.gateway.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class SignoffPathId {

    public SignoffPathId(String companyName, Long pathId) {
        this.key = new SignoffPathIdKey(companyName, pathId);
    }

    public SignoffPathId() {
        // Default constructor
    }

    @EmbeddedId
    private SignoffPathIdKey key;

    public SignoffPathIdKey getKey() {
        return key;
    }

    public void setKey(SignoffPathIdKey key) {
        this.key = key;
    }
}
