package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathId;
import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SignoffPathIdRepository extends JpaRepository<SignoffPathId, SignoffPathKey> {

    SignoffPathId findByKeyCompanyName(String companyName);

    @Query(
            "UPDATE SignoffPathId spid " +
            "SET spid.key.pathId=spid.key.pathId+1 " +
            "WHERE spid.key.companyName=:companyName"
    )
    @Modifying
    @Transactional
    void incrementPathId(@Param("companyName") String companyName);
}
