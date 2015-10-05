package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentRevisionLikeKey implements Serializable {

    private String companyName;
    private String documentId;
    private String revisionId;
    private Long userId;

    public DocumentRevisionLikeKey() {
        // Default Constructor
    }

    public DocumentRevisionLikeKey(String companyName, String documentId, String revisionId, Long userId) {
        this.companyName = companyName;
        this.documentId = documentId;
        this.revisionId = revisionId;
        this.userId = userId;
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
}
