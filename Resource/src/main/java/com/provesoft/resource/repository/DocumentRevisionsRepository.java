package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.Document.DocumentRevisionsKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRevisionsRepository extends JpaRepository<DocumentRevisions, DocumentRevisionsKey> {

    List<DocumentRevisions> findByKeyCompanyNameAndKeyDocumentIdOrderByKeyRevisionIdDesc(String companyName, String documentId);

    DocumentRevisions findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(String companyName, String documentId, String revisionId);
}
