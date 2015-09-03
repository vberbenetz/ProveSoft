package com.provesoft.resource.entity.SignoffPath;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity is a copy of a SignoffPathTemplateStep applied directly to a specific DocumentRevision.
 * Any changes done to it's twin SignoffPathTemplateStep will not be applied until after another copy is made.
 * This contains all information for a specific step of the entire SignoffPathStep chain.
 */
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
    private Date approvalDate;

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

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
