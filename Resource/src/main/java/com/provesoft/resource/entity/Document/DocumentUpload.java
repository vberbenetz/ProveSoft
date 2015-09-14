package com.provesoft.resource.entity.Document;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * Entity holds file metadata and file data of file uploads
 */
@Entity
public class DocumentUpload {

    public DocumentUpload(String companyName,
                          String documentId,
                          String fileId,
                          String filename,
                          String mimeType,
                          String revision,
                          Boolean redline) {

        this.key = new DocumentUploadKey(companyName, documentId, revision, redline);
        this.fileId = fileId;
        this.filename = filename;
        this.mimeType = mimeType;
    }

    public DocumentUpload() {
        // Default Constructor
    }

    @EmbeddedId
    private DocumentUploadKey key;

    private String filename;

    private String mimeType;

    private String fileId;

    public DocumentUploadKey getKey() {
        return key;
    }

    public void setKey(DocumentUploadKey key) {
        this.key = key;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
