package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface DocumentTypeIdRepository extends JpaRepository<DocumentTypeId, Long> {

    DocumentTypeId findByDocumentTypeId(Long documentTypeId);

    @Query(
            "UPDATE DocumentTypeId d " +
            "SET d.currentSuffixId=d.currentSuffixId+1 " +
            "WHERE d.documentTypeId=:documentTypeId"
    )
    @Modifying
    @Transactional
    void incrementSuffixId(@Param("documentTypeId") Long documentTypeId);
}
