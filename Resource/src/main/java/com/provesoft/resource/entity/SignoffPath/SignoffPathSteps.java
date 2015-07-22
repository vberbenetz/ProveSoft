package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;

@Entity
public class SignoffPathSteps {

    public SignoffPathSteps (String companyName, Long pathId, String action, UserDetails user) {
        this.companyName = companyName;
        this.pathId = pathId;
        this.action = action;
        this.user = user;
    }

    public SignoffPathSteps() {
        // Default Constructor
    }

    @Id
    @GeneratedValue
    Long id;

    private String companyName;
    private Long pathId;
    private String action;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
