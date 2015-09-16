package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathTemplateSteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SignoffPathTemplateStepsRepository extends JpaRepository<SignoffPathTemplateSteps, Long> {

    SignoffPathTemplateSteps findByCompanyNameAndPathIdAndId(String companyName, Long pathId, Long id);

    List<SignoffPathTemplateSteps> findByIdIn(List<Long> ids);

    List<SignoffPathTemplateSteps> findByCompanyNameAndPathIdOrderByIdAsc(String companyName, Long pathId);

    @Query(
            "DELETE " +
            "FROM SignoffPathTemplateSteps spts " +
            "WHERE spts.companyName=:companyName " +
            "AND spts.pathId=:pathId"
    )
    @Modifying
    @Transactional
    void deleteTemplateForPath(@Param("companyName") String companyName,
                               @Param("pathId") Long pathId);
}
