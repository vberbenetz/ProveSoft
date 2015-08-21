package com.provesoft.resource.entity.Document;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DocumentLikeKey implements Serializable {

    public DocumentLikeKey(String companyName, Long documentCommentId, Long userId) {
        this.comanyName = companyName;
        this.documentCommentId = documentCommentId;
        this.userId = userId;
    }

    public DocumentLikeKey() {
        // Default Constructor
    }

    private String comanyName;
    private Long documentCommentId;
    private Long userId;

    public String getComanyName() {
        return comanyName;
    }

    public void setComanyName(String comanyName) {
        this.comanyName = comanyName;
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
