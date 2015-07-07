package com.provesoft.resource.entity;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
public class Document {

    public Document(String companyName, String title, DocumentType documentType, Organizations organization) {
        this.companyName = companyName;
        this.title = title;
        this.documentType = documentType;
        this.organization = organization;
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

    @ManyToOne
    @JoinColumn
    private Organizations organization;

    private String title;

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

    public Organizations getOrganization() {
        return organization;
    }

    public void setOrganization(Organizations organization) {
        this.organization = organization;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
