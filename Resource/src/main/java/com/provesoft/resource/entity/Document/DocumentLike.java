package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class DocumentLike {

    public DocumentLike(String companyName, Long documentCommentId, Long userId) {
        this.key = new DocumentLikeKey(companyName, documentCommentId, userId);
    }

    public DocumentLike() {
        // Default Constructor
    }

    @EmbeddedId
    private DocumentLikeKey key;

    public DocumentLikeKey getKey() {
        return key;
    }

    public void setKey(DocumentLikeKey key) {
        this.key = key;
    }
}
