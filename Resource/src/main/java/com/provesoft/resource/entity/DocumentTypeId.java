package com.provesoft.resource.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/*
    This entity exists to maintain the current numeric suffix id for a new document of documentType.
    The reason for a separate entity is to prevent the Isolation.SERIALIZABLE property from locking down the
    entire DocumentType entity, and only lock down this one when a new document is being generated.
 */

@Entity
public class DocumentTypeId {

    public DocumentTypeId (Long documentTypeId, Long currentSuffixId) {
        this.documentTypeId = documentTypeId;
        this.currentSuffixId = currentSuffixId;
    }

    public DocumentTypeId() {
        // Public constructor
    }

    @Id
    Long documentTypeId;

    Long currentSuffixId;

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public Long getCurrentSuffixId() {
        return currentSuffixId;
    }

    public void setCurrentSuffixId(Long currentSuffixId) {
        this.currentSuffixId = currentSuffixId;
    }
}
