package com.provesoft.resource.entity.Document;

import javax.persistence.*;
import java.util.Date;

@Entity
public class DocumentComment {

    public DocumentComment(String companyName, Long userId, String documentId, Date date, String message){
        this.companyName = companyName;
        this.userId = userId;
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

    private Long userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
