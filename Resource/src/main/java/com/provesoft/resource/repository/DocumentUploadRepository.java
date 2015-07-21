package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentUpload;
import com.provesoft.resource.entity.Document.DocumentUploadKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, DocumentUploadKey> {

    DocumentUpload findByKeyCompanyNameAndKeyDocumentIdAndKeyRedline(String companyName, String documentId, Boolean redline);
}
