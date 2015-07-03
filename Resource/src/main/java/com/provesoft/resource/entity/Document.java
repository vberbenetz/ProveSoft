package com.provesoft.resource.entity;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
public class Document {

    public Document(String companyName, DocumentType documentType, Long organizationId) {
        this.companyName = companyName;
        this.documentType = documentType;
        this.organizationId = organizationId;
    }

    public Document() {
        // Default constructor
    }

    @Id
    private String id;

    private String companyName;

    @ManyToOne
    @JoinColumn
    private DocumentType documentType;

    private Long organizationId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
