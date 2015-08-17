package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignoffPathTemplateStepsRepository extends JpaRepository<SignoffPathTemplateSteps, Long> {

    SignoffPathTemplateSteps findByCompanyNameAndPathIdAndId(String companyName, Long pathId, Long id);

    List<SignoffPathTemplateSteps> findByIdIn(List<Long> ids);

    List<SignoffPathTemplateSteps> findByCompanyNameAndPathIdOrderByIdAsc(String companyName, Long pathId);
}
