package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class RevisionApprovalStatus {

    public RevisionApprovalStatus(String companyName, String documentId, String signoffPathSeq) {
        this.key = new RevisionApprovalStatusKey(companyName, documentId);
        this.signoffPathSeq = signoffPathSeq;
        this.approvedSeq = "";
    }

    public RevisionApprovalStatus() {
        // Default Constructor
    }

    @EmbeddedId
    private RevisionApprovalStatusKey key;

    private String signoffPathSeq;
    private String approvedSeq;

    public RevisionApprovalStatusKey getKey() {
        return key;
    }

    public void setKey(RevisionApprovalStatusKey key) {
        this.key = key;
    }

    public String getSignoffPathSeq() {
        return signoffPathSeq;
    }

    public void setSignoffPathSeq(String signoffPathSeq) {
        this.signoffPathSeq = signoffPathSeq;
    }

    public String getApprovedSeq() {
        return approvedSeq;
    }

    public void setApprovedSeq(String approvedSeq) {
        this.approvedSeq = approvedSeq;
    }
}
