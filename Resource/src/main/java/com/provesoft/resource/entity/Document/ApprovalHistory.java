package com.provesoft.resource.entity.Document;

import com.provesoft.resource.utils.SystemHelpers;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ApprovalHistory {

    public ApprovalHistory(Long stepId, String companyName, Long documentId, String revisionId, Long userId, String date) {
        this.stepId = stepId;
        this.companyName = companyName;
        this.documentId = documentId;
        this.revisionId = revisionId;
        this.userId = userId;
        this.date = date;
    }

    public ApprovalHistory() {
        // Public Constructor
    }

    @Id
    private Long stepId;

    private String companyName;
    private Long documentId;
    private String revisionId;
    private Long userId;
    private String date;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
