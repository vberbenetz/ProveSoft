package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentUploadKey implements Serializable {

    public DocumentUploadKey(String companyName, String documentId) {
        this.companyName = companyName;
        this.documentId = documentId;
    }

    public DocumentUploadKey() {
        // Default Constructor
    }

    private String companyName;
    private String documentId;

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
}
