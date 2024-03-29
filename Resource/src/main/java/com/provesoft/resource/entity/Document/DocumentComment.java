package com.provesoft.resource.entity.Document;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity encompasses all data regarding a comment made by a user for a Document
 */
@Entity
public class DocumentComment {

    public DocumentComment(String companyName, UserDetails user, String documentId, Date date, String message){
        this.companyName = companyName;
        this.user = user;
        this.documentId = documentId;
        this.date = date;
        this.message = message;
    }

    public DocumentComment() {
        // Default Constructor
    }

    @Id
    @GeneratedValue
    private Long id;

    private String companyName;

    private Long parentCommentId;

    @ManyToOne
    @JoinColumn
    private UserDetails user;

    private String documentId;
    private Date date;

    @Lob
    private String message;

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

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
