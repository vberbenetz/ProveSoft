package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProfilePictureKey implements Serializable {

    public ProfilePictureKey(String companyName, Long userId) {
        this.companyName = companyName;
        this.userId = userId;
    }

    public ProfilePictureKey() {
        // Default constructor
    }

    private String companyName;
    private Long userId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
