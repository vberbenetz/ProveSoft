package com.provesoft.resource.repository;


import com.provesoft.resource.entity.RoleUser;
import com.provesoft.resource.entity.RoleUserKey;
import com.provesoft.resource.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleUserRepository extends JpaRepository<RoleUser, RoleUserKey> {

    List<RoleUser> findByKeyCompanyNameAndKeyRoleId(String company, Long roleId);

    List<RoleUser> findByKeyCompanyNameAndKeyUserId(String companyName, Long userId);
}
