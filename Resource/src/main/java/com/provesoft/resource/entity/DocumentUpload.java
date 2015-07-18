package com.provesoft.resource.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class DocumentUpload {

    public DocumentUpload(String companyName,
                          String documentId,
                          byte[] file,
                          String filename,
                          String mimeType,
                          Boolean redline) {

        this.key = new DocumentUploadKey(companyName, documentId);
        this.file = file;
        this.filename = filename;
        this.mimeType = mimeType;
        this.redline = redline;
    }

    public DocumentUpload() {
        // Default Constructor
    }

    @EmbeddedId
    private DocumentUploadKey key;

    @Lob
    private byte[] file;

    private String filename;

    private String mimeType;

    private Boolean redline;

    public DocumentUploadKey getKey() {
        return key;
    }

    public void setKey(DocumentUploadKey key) {
        this.key = key;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
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

    public Boolean getRedline() {
        return redline;
    }

    public void setRedline(Boolean redline) {
        this.redline = redline;
    }
}
