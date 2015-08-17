package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;

@Entity
public class SignoffPathSteps {

    public SignoffPathSteps(String companyName, String documentId, Long pathId, Long templateId, String action, UserDetails user) {
        this.companyName = companyName;
        this.documentId = documentId;
        this.pathId = pathId;
        this.templateId = templateId;
        this.action = action;
        this.approved = false;
        this.user = user;
    }

    public SignoffPathSteps() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    private Long id;

    private String companyName;
    private String documentId;
    private Long pathId;
    private Long templateId;
    private String action;
    private Boolean approved;

    @ManyToOne
    @JoinColumn
    private UserDetails user;


    public Long getId() { return id; }

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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
