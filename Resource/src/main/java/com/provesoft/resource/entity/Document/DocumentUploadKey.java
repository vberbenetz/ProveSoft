package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentUploadKey implements Serializable {

    public DocumentUploadKey(String companyName, String documentId, String revision, Boolean redline) {
        this.companyName = companyName;
        this.documentId = documentId;
        this.revision = revision;
        this.redline = redline;
    }

    public DocumentUploadKey() {
        // Default Constructor
    }

    private String companyName;
    private String documentId;
    private String revision;
    private Boolean redline;

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

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Boolean getRedline() {
        return redline;
    }

    public void setRedline(Boolean redline) {
        this.redline = redline;
    }
}
