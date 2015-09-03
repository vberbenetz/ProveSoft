package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity holds information about mapping a DocumentComment Like to a user
 */
@Entity
public class DocumentCommentLike {

    public DocumentCommentLike(String companyName, Long documentCommentId, Long userId) {
        this.key = new DocumentCommentLikeKey(companyName, documentCommentId, userId);
    }

    public DocumentCommentLike() {
        // Default Constructor
    }

    @EmbeddedId
    private DocumentCommentLikeKey key;

    public DocumentCommentLikeKey getKey() {
        return key;
    }

    public void setKey(DocumentCommentLikeKey key) {
        this.key = key;
    }
}
