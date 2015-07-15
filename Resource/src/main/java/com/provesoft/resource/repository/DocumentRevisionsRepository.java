package com.provesoft.resource.repository;

import com.provesoft.resource.entity.DocumentRevisions;
import com.provesoft.resource.entity.DocumentRevisionsKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRevisionsRepository extends JpaRepository<DocumentRevisions, DocumentRevisionsKey> {

    List<DocumentRevisions> findByKeyCompanyNameAndKeyDocumentId(String companyName, String documentId);
}
