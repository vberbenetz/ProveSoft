package com.provesoft.resource.entity.Document;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity maintains the history of SignoffPathStep approvals
 */
@Entity
public class ApprovalHistory {

    public ApprovalHistory(String companyName,
                           String documentId,
                           String revisionId,
                           DocumentRevisions documentRevision,
                           String action,
                           UserDetails user,
                           Date date) {

        this.companyName = companyName;
        this.documentId = documentId;
        this.revisionId = revisionId;
        this.documentRevision = documentRevision;
        this.action = action;
        this.user = user;
        this.date = date;
    }

    public ApprovalHistory() {
        // Public Constructor
    }

    @Id
    @GeneratedValue
    Long id;

    private String companyName;
    private String documentId;
    private String revisionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "companyName_revision", referencedColumnName = "companyName"),
            @JoinColumn(name = "documentId_revision", referencedColumnName = "documentId"),
            @JoinColumn(name = "revisionId_revision", referencedColumnName = "revisionId")
    })
    private DocumentRevisions documentRevision;

    private String action;
    private Date date;

    @ManyToOne
    @JoinColumn
    private UserDetails user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public DocumentRevisions getDocumentRevision() {
        return documentRevision;
    }

    public void setDocumentRevision(DocumentRevisions documentRevision) {
        this.documentRevision = documentRevision;
    }
}
