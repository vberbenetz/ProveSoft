package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class DocumentRevisions {

    public DocumentRevisions(String companyName,
                             String documentId,
                             String revisionId,
                             String changeReason,
                             Long changeUserId,
                             String changeDate,
                             Boolean redlineDocPresent) {

        this.key = new DocumentRevisionsKey(companyName, documentId, revisionId);
        this.changeReason = changeReason;
        this.changeUserId = changeUserId;
        this.changeDate = changeDate;
        this.redlineDocPresent = redlineDocPresent;
    }

    public DocumentRevisions() {
        // Public Constructor
    }

    @EmbeddedId
    private DocumentRevisionsKey key;

    private String changeReason;
    private Long changeUserId;
    private String changeDate;
    private Boolean redlineDocPresent;

    public DocumentRevisionsKey getKey() {
        return key;
    }

    public void setKey(DocumentRevisionsKey key) {
        this.key = key;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Long getChangeUserId() {
        return changeUserId;
    }

    public void setChangeUserId(Long changeUserId) {
        this.changeUserId = changeUserId;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public Boolean getRedlineDocPresent() {
        return redlineDocPresent;
    }

    public void setRedlineDocPresent(Boolean redlineDocPresent) {
        this.redlineDocPresent = redlineDocPresent;
    }
}
