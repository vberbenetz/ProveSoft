package com.provesoft.resource.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentRevisionsKey implements Serializable {

    public DocumentRevisionsKey (String companyName, String documentId, String revisionId) {
        this.companyName = companyName;
        this.documentId = documentId;
        this.revisionId = revisionId;
    }

    public DocumentRevisionsKey() {
        // Public constructor
    }

    private String companyName;
    private String documentId;
    private String revisionId;

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
}
