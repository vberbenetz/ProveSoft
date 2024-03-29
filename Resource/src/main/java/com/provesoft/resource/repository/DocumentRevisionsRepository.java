package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentRevisions;
import com.provesoft.resource.entity.Document.DocumentRevisionsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentRevisionsRepository extends JpaRepository<DocumentRevisions, DocumentRevisionsKey> {

    List<DocumentRevisions> findByKeyCompanyNameAndKeyDocumentIdOrderByKeyRevisionIdDesc(String companyName, String documentId);

    DocumentRevisions findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionId(String companyName, String documentId, String revisionId);

    List<DocumentRevisions> findFirst5ByKeyCompanyNameOrderByChangeDateDesc(String companyName);

    @Query(
            "SELECT r " +
            "FROM DocumentRevisions r " +
            "WHERE r.key.companyName=:companyName " +
            "AND r.key.documentId IN :documentIds " +
            "ORDER BY r.key.documentId ASC, r.key.revisionId ASC"
    )
    List<DocumentRevisions> findRevisionByKeyCompanyNameAndKeyDocumentIdIn(@Param("companyName") String companyName,
                                                                           @Param("documentIds") String[] documentIds);

    @Query(
            "DELETE FROM DocumentRevisions r " +
            "WHERE r.key.companyName=:companyName " +
            "AND r.key.documentId=:documentId " +
            "AND r.key.revisionId=:revisionId"
    )
    @Modifying
    @Transactional
    void deleteByCompanyAndDocumentIdAndRevisionId(@Param("companyName") String companyName,
                                                   @Param("documentId") String documentId,
                                                   @Param("revisionId") String revisionId);
}
