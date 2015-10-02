package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentUpload;
import com.provesoft.resource.entity.Document.DocumentUploadKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, DocumentUploadKey> {

    DocumentUpload findByKeyCompanyNameAndKeyDocumentIdAndKeyRevisionAndKeyRedline(String companyName, String documentId, String revision, Boolean redline);

    List<DocumentUpload> findByKeyCompanyNameAndKeyDocumentIdAndKeyRevision(String companyName, String documentId, String revision);

    @Query(
            "UPDATE DocumentUpload du " +
            "SET du.key.revision=:newRevId " +
            "WHERE du.key.companyName=:companyName " +
            "AND du.key.revision=:tempRevId"
    )
    @Modifying
    @Transactional
    void updateRevisionId(@Param("companyName") String companyName,
                          @Param("tempRevId") String tempRevId,
                          @Param("newRevId") String newRevId);

    @Query(
            "DELETE FROM DocumentUpload du " +
            "WHERE du.key.companyName=:companyName " +
            "AND du.key.documentId=:documentId " +
            "AND du.key.revision=:revision "
    )
    @Modifying
    @Transactional
    void deleteUploads(@Param("companyName") String companyName,
                       @Param("documentId") String documentId,
                       @Param("revision") String revision);

}
