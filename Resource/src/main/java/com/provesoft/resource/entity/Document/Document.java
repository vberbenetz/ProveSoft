package com.provesoft.resource.entity.Document;

import com.provesoft.resource.entity.Organizations;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity holds all data for the Document object
 */
@Entity
public class Document {

    public Document(String companyName,
                    String title,
                    DocumentType documentType,
                    Organizations organization) {

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

    private Long signoffPathId;

    private String title;
    private String revision;
    private String state;
    private Date date;

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

    public Long getSignoffPathId() {
        return signoffPathId;
    }

    public void setSignoffPathId(Long signoffPathId) {
        this.signoffPathId = signoffPathId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
