package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathSteps;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignoffPathStepsRepository extends JpaRepository<SignoffPathSteps, Long> {

    SignoffPathSteps findByCompanyNameAndPathId(String companyName, Long pathId);

}
