package com.provesoft.resource.entity.Document;

import javax.persistence.*;

@Entity
public class FavouriteDocuments {

    public FavouriteDocuments(String companyName, String email, Document document) {
        this.key = new FavouriteDocumentKey(companyName, email, document.getId());
        this.document = document;
    }

    public FavouriteDocuments() {
        // Default Constructor
    }

    @EmbeddedId
    private FavouriteDocumentKey key;

    @ManyToOne
    @JoinColumn
    private Document document;

    public FavouriteDocumentKey getKey() {
        return key;
    }

    public void setKey(FavouriteDocumentKey key) {
        this.key = key;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
