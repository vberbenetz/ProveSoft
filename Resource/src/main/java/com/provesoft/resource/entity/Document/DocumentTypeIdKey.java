package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentTypeIdKey implements Serializable {

    public DocumentTypeIdKey(String companyName, Long documentTypeId) {
        this.companyName = companyName;
        this.documentTypeId = documentTypeId;
    }

    public DocumentTypeIdKey() {
        // Public Constructor
    }

    private String companyName;
    private Long documentTypeId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }
}
