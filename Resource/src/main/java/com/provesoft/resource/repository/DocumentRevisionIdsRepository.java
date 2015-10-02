package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentRevisionIds;
import com.provesoft.resource.entity.Document.DocumentRevisionIdsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentRevisionIdsRepository extends JpaRepository<DocumentRevisionIds, DocumentRevisionIdsKey> {

    DocumentRevisionIds findByKeyCompanyNameAndKeyDocumentId(String companyName, String documentId);

    @Query(
            "UPDATE DocumentRevisionIds dri " +
            "SET dri.revisionId=:nextRevId " +
            "WHERE dri.key.companyName=:companyName " +
            "AND dri.key.documentId=:documentId"
    )
    @Transactional
    @Modifying
    void incrementRevId(@Param("companyName") String companyName,
                        @Param("documentId") String documentId,
                        @Param("nextRevId") String nextRevId);

    @Query(
            "DELETE FROM DocumentRevisionIds dri " +
            "WHERE dri.key.companyName=:companyName " +
            "AND dri.key.documentId=:documentId"
    )
    @Transactional
    @Modifying
    void deleteByCompanyAndDocumentId(@Param("companyName") String companyName,
                                      @Param("documentId") String documentId);
}
