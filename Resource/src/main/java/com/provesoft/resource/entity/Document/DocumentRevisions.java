package com.provesoft.resource.entity.Document;

import com.provesoft.resource.entity.UserDetails;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class DocumentRevisions {

    public DocumentRevisions(String companyName,
                             String documentId,
                             String revisionId,
                             String changeReason,
                             UserDetails changeUser,
                             Date changeDate,
                             Boolean redlineDocPresent) {

        this.key = new DocumentRevisionsKey(companyName, documentId, revisionId);
        this.changeReason = changeReason;
        this.changeUser = changeUser;
        this.changeDate = changeDate;
        this.redlineDocPresent = redlineDocPresent;
    }

    public DocumentRevisions() {
        // Public Constructor
    }

    @EmbeddedId
    private DocumentRevisionsKey key;

    private String changeReason;

    @ManyToOne
    @JoinColumn
    private UserDetails changeUser;

    private Date changeDate;
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

    public UserDetails getChangeUser() {
        return changeUser;
    }

    public void setChangeUser(UserDetails changeUser) {
        this.changeUser = changeUser;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public Boolean getRedlineDocPresent() {
        return redlineDocPresent;
    }

    public void setRedlineDocPresent(Boolean redlineDocPresent) {
        this.redlineDocPresent = redlineDocPresent;
    }
}
