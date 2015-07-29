package com.provesoft.gateway.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class SignoffPathIdKey implements Serializable {

    public SignoffPathIdKey(String companyName, Long pathId) {
        this.companyName = companyName;
        this.pathId = pathId;
    }

    public SignoffPathIdKey(){
        // Public constructor
    }

    private String companyName;
    private Long pathId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getPathId() {
        return pathId;
    }

    public void setPathId(Long pathId) {
        this.pathId = pathId;
    }
}
