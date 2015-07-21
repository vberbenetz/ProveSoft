package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/*
    This entity exists to maintain the current revision id for the document.
    It is segregated to prevent locking down the document revision table
    because Isolation.SERIALIZABLE is being used.
 */

@Entity
public class DocumentRevisionIds {

    public DocumentRevisionIds (String companyName, String documentId, String revisionId) {
        this.key = new DocumentRevisionIdsKey(companyName, documentId);
        this.revisionId = revisionId;
    }

    public DocumentRevisionIds() {
        // Public constructor
    }

    @EmbeddedId
    private DocumentRevisionIdsKey key;

    private String revisionId;

    public DocumentRevisionIdsKey getKey() {
        return key;
    }

    public void setKey(DocumentRevisionIdsKey key) {
        this.key = key;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }
}
