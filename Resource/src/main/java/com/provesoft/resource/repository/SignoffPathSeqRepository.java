package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import com.provesoft.resource.entity.SignoffPath.SignoffPathSeq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignoffPathSeqRepository extends JpaRepository<SignoffPathSeq, SignoffPathKey> {

    SignoffPathSeq findByKeyCompanyNameAndKeyPathId(String companyName, Long pathId);
}
