package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RevisionApprovalStatusKey implements Serializable {

    public RevisionApprovalStatusKey(String companyName, String documentId) {
        this.companyName = companyName;
        this.documentId = documentId;
    }

    public RevisionApprovalStatusKey() {
        // Default constructor
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
