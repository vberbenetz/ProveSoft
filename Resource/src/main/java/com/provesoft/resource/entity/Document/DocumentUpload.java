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
                          byte[] file,
                          String filename,
                          String mimeType,
                          String revision,
                          Boolean redline) {

        this.key = new DocumentUploadKey(companyName, documentId, revision, redline);
        this.file = file;
        this.filename = filename;
        this.mimeType = mimeType;
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

}
