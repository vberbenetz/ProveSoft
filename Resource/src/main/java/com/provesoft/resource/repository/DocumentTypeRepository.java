package com.provesoft.resource.repository;

import com.provesoft.resource.entity.Document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    List<DocumentType> findByCompanyName(String companyName);

    DocumentType findByCompanyNameAndId(String companyName, Long id);

    Long countByCompanyNameAndDocumentPrefix(String companyName, String documentPrefix);

    Long countByCompanyNameAndName(String companyName, String name);

    @Query(
            "UPDATE DocumentType d " +
            "SET d.currentSuffix=:newSuffix " +
            "WHERE d.id=:id"
    )
    @Modifying
    @Transactional
    void updateCurrentSuffix(@Param("id") Long id, @Param("newSuffix") Long newSuffix);

}
