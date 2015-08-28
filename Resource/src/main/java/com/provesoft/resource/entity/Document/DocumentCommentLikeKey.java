package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentCommentLikeKey implements Serializable {

    public DocumentCommentLikeKey(String companyName, Long documentCommentId, Long userId) {
        this.companyName = companyName;
        this.documentCommentId = documentCommentId;
        this.userId = userId;
    }

    public DocumentCommentLikeKey() {
        // Default Constructor
    }

    private String companyName;
    private Long documentCommentId;
    private Long userId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getDocumentCommentId() {
        return documentCommentId;
    }

    public void setDocumentCommentId(Long documentCommentId) {
        this.documentCommentId = documentCommentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
