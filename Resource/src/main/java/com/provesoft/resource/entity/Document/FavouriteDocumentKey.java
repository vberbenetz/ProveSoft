package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class FavouriteDocumentKey implements Serializable {

    public FavouriteDocumentKey(String companyName, String email, String documentId) {
        this.companyName = companyName;
        this.email = email;
        this.documentId = documentId;
    }

    public FavouriteDocumentKey() {
        // Default constructor
    }

    private String companyName;
    private String email;
    private String documentId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
