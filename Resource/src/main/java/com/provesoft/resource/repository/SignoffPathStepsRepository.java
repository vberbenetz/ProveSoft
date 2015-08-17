package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignoffPathStepsRepository extends JpaRepository<SignoffPathSteps, Long> {

    List<SignoffPathSteps> findByCompanyNameAndDocumentIdOrderByIdAsc(String companyName, String DocumentId);

    List<SignoffPathSteps> findByCompanyNameAndDocumentIdAndApprovedOrderByIdAsc(String companyName, String documentId, Boolean approved);

    Long countByCompanyNameAndDocumentIdAndId(String companyName, String documentId, Long id);

    List<SignoffPathSteps> deleteByCompanyNameAndDocumentId(String companyName, String documentId);
}
