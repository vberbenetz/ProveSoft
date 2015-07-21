package com.provesoft.resource.repository;

import com.provesoft.resource.entity.SignoffPath.SignoffPath;
import com.provesoft.resource.entity.SignoffPath.SignoffPathKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignoffPathRepository extends JpaRepository<SignoffPath, SignoffPathKey> {

    SignoffPath findByKeyCompanyNameAndKeyPathId(String companyName, Long pathId);

    List<SignoffPath> findFirst10ByKeyCompanyNameAndKeyPathIdLikeOrderByKeyPathIdAsc(String companyName, Long pathId);

    List<SignoffPath> findFirst10ByKeyCompanyNameAndNameLikeOrderByNameAsc(String companyName, String name);

    List<SignoffPath> findFirst10ByKeyCompanyNameOrderByKeyPathIdAsc(String companyName);
}
