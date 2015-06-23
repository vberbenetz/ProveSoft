package com.provesoft.resource.repository;


import com.provesoft.resource.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolesRepository extends JpaRepository<Roles, Long> {

    List<Roles> findByCompanyName(String companyName);

    List<Roles> findByCompanyNameAndRoleIdIn(String company, List<Long> roleIds);
}
