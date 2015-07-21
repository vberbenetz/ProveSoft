package com.provesoft.resource.entity.SignoffPath;

import javax.persistence.Entity;

@Entity
public class SignoffPathSeq {

    public SignoffPathSeq(String companyName, Long pathId, String pathSequence) {
        this.key = new SignoffPathKey(companyName, pathId);
        this.pathSequence = pathSequence;
    }

    public SignoffPathSeq() {
        // Default Constructor
    }

    private SignoffPathKey key;

    private String pathSequence;

    public SignoffPathKey getKey() {
        return key;
    }

    public void setKey(SignoffPathKey key) {
        this.key = key;
    }

    public String getPathSequence() {
        return pathSequence;
    }

    public void setPathSequence(String pathSequence) {
        this.pathSequence = pathSequence;
    }

}
