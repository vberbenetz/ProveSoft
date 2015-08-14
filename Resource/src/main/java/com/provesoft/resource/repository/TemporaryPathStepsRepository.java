package com.provesoft.resource.repository;


import com.provesoft.resource.entity.SignoffPath.TemporaryPathSteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemporaryPathStepsRepository extends JpaRepository<TemporaryPathSteps, Long> {

    List<TemporaryPathSteps> findByCompanyNameAndDocumentIdAndPathIdOrderByIdAsc(String companyName, String documentId, Long pathId);
}
