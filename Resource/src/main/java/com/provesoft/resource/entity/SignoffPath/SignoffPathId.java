package com.provesoft.resource.entity.SignoffPath;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class SignoffPathId {

    public SignoffPathId (String companyName, Long pathId) {
        this.key = new SignoffPathKey(companyName, pathId);
    }

    @EmbeddedId
    private SignoffPathKey key;

    public SignoffPathKey getKey() {
        return key;
    }

    public void setKey(SignoffPathKey key) {
        this.key = key;
    }
}
