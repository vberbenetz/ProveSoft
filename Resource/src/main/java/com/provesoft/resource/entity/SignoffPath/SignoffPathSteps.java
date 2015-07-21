package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SignoffPathSteps {

    public SignoffPathSteps (String companyName, Long pathId, String stepSequence, UserDetails user) {
        this.companyName = companyName;
        this.pathId = pathId;
        this.stepSequence = stepSequence;
        this.user = user;
    }

    public SignoffPathSteps() {
        // Default Constructor
    }

    @Id
    Long id;

    private String companyName;
    private Long pathId;
    private String stepSequence;

    @ManyToOne
    @JoinColumn
    private UserDetails user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStepSequence() {
        return stepSequence;
    }

    public void setStepSequence(String stepSequence) {
        this.stepSequence = stepSequence;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
