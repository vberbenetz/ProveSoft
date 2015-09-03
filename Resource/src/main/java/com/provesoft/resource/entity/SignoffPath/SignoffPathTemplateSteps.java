package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.persistence.*;

/**
 * Entity is a template which is copied to create steps applied directly to a specific DocumentRevision.
 * This template is created in the admin panel.
 */
@Entity
public class SignoffPathTemplateSteps {

    public SignoffPathTemplateSteps(String companyName, Long pathId, String action, UserDetails user) {
        this.companyName = companyName;
        this.pathId = pathId;
        this.action = action;
        this.user = user;
    }

    public SignoffPathTemplateSteps() {
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
