package com.provesoft.resource.repository;

import com.provesoft.resource.entity.OrgUser;
import com.provesoft.resource.entity.OrgUserKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrgUserRepository extends JpaRepository<OrgUser, OrgUserKey> {

    List<OrgUser> findByKeyCompanyNameAndKeyUserId(String companyName, Long userId);

    List<OrgUser> findByKeyCompanyNameAndKeyOrganizationId(String companyName, Long organizationId);
}
