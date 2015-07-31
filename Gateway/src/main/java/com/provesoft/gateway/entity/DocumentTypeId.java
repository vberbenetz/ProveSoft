package com.provesoft.gateway.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/*
    This entity exists to maintain the current numeric suffix id for a new document of documentType.
    The reason for a separate entity is to prevent the Isolation.SERIALIZABLE property from locking down the
    entire DocumentType entity, and only lock down this one when a new document is being generated.
 */

@Entity
public class DocumentTypeId {

    public DocumentTypeId (String companyName, Long documentTypeId, Long currentSuffixId) {
        this.key = new DocumentTypeIdKey(companyName, documentTypeId);
        this.currentSuffixId = currentSuffixId;
    }

    public DocumentTypeId() {
        // Public constructor
    }

    @EmbeddedId
    private DocumentTypeIdKey key;

    private Long currentSuffixId;

    public DocumentTypeIdKey getKey() {
        return key;
    }

    public void setKey(DocumentTypeIdKey key) {
        this.key = key;
    }

    public Long getCurrentSuffixId() {
        return currentSuffixId;
    }

    public void setCurrentSuffixId(Long currentSuffixId) {
        this.currentSuffixId = currentSuffixId;
    }
}
