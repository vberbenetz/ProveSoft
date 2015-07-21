package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.Organizations;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SignoffPath {

    public SignoffPath(String companyName, Long pathId, String name, Organizations organization) {
        this.key = new SignoffPathKey(companyName, pathId);
        this.name = name;
        this.organization = organization;
    }

    public SignoffPath() {
        // Default Constructor
    }

    @EmbeddedId
    private SignoffPathKey key;

    private String name;

    @ManyToOne
    @JoinColumn
    private Organizations organization;

    public SignoffPathKey getKey() {
        return key;
    }

    public void setKey(SignoffPathKey key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organizations getOrganization() {
        return organization;
    }

    public void setOrganization(Organizations organization) {
        this.organization = organization;
    }
}
