package com.provesoft.resource.entity.Document;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ApprovalNotification {

    public ApprovalNotification(String companyName, Long userId, Long stepId, String documentId) {
        this.companyName = companyName;
        this.userId = userId;
        this.stepId = stepId;
        this.documentId = documentId;
    }

    public ApprovalNotification() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    private Long id;

    private String companyName;
    private Long userId;
    private Long stepId;
    private String documentId;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
