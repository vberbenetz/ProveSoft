package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;

@Entity
public class TemporaryPathSteps {

    public TemporaryPathSteps(String companyName, String documentId, Long pathId, String action, UserDetails user) {
        this.companyName = companyName;
        this.documentId = documentId;
        this.pathId = pathId;
        this.action = action;
        this.user = user;
    }

    public TemporaryPathSteps() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long id;

    private String companyName;
    private String documentId;
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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
}
