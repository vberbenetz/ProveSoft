package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentRevisionIdsKey implements Serializable {

    public DocumentRevisionIdsKey (String companyName, String documentId) {
        this.companyName = companyName;
        this.documentId = documentId;
    }

    public DocumentRevisionIdsKey() {
        // Public Constructor
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
