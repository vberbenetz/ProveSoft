package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class DocumentRevisionLike {

    @EmbeddedId
    DocumentRevisionLikeKey key;

    public DocumentRevisionLikeKey getKey() {
        return key;
    }

    public void setKey(DocumentRevisionLikeKey key) {
        this.key = key;
    }
}
