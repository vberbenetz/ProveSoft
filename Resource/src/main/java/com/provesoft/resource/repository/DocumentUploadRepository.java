package com.provesoft.resource.repository;

import com.provesoft.resource.entity.DocumentUpload;
import com.provesoft.resource.entity.DocumentUploadKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, DocumentUploadKey> {

    DocumentUpload findByKeyCompanyNameAndKeyDocumentIdAndKeyRedline(String companyName, String documentId, Boolean redline);
}
