package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignoffPathStepsRepository extends JpaRepository<SignoffPathSteps, Long> {

    List<SignoffPathSteps> findByCompanyNameAndPathIdOrderByIdAsc(String companyName, Long pathId);

    SignoffPathSteps findByCompanyNameAndPathIdAndId(String companyName, Long pathId, Long id);
}
