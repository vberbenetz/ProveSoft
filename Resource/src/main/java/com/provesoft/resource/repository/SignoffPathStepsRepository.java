package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SignoffPathStepsRepository extends JpaRepository<SignoffPathSteps, Long> {

    List<SignoffPathSteps> findByCompanyNameAndDocumentIdOrderByIdAsc(String companyName, String DocumentId);

    List<SignoffPathSteps> findByCompanyNameAndDocumentIdAndApprovedOrderByIdAsc(String companyName, String documentId, Boolean approved);

    @Query (
            "DELETE FROM SignoffPathSteps s " +
            "WHERE s.companyName=:companyName " +
            "AND s.documentId=:documentId"
    )
    @Modifying
    @Transactional
    void deleteSteps(@Param("companyName") String companyName,
                     @Param("documentId") String documentId);
}
