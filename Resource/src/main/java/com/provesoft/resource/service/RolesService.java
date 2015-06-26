package com.provesoft.resource.service;

import com.provesoft.resource.entity.RoleUser;
import com.provesoft.resource.entity.Roles;
import com.provesoft.resource.repository.RoleUserRepository;
import com.provesoft.resource.repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolesService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    RoleUserRepository roleUserRepository;

    public List<Roles> findByCompanyName(String companyName) {
        return rolesRepository.findByCompanyName(companyName);
    }

    public List<Roles> findByCompanyNameAndRoleIdList(String company, List<Long> roleIds) {
        return rolesRepository.findByCompanyNameAndRoleIdIn(company, roleIds);
    }

    public void saveRole(Roles role) {
        rolesRepository.save(role);
    }


    /* ---------------------- RoleUser -------------------- */

    public List<RoleUser> findByCompanyNameAndUserId (String companyName, Long userId) {
        return roleUserRepository.findByKeyCompanyNameAndKeyUserId(companyName, userId);
    }

    public List<RoleUser> findByCompanyNameAndRoleId(String company, Long roleId) {
        return roleUserRepository.findByKeyCompanyNameAndKeyRoleId(company, roleId);
    }

    public void saveRoleUser(List<RoleUser> roleUser) { roleUserRepository.save(roleUser); }

    public void deleteRoleUser(Long roleId, Long userId, String companyName) {
        roleUserRepository.deleteRoleUser(roleId, userId, companyName);
    }

    public void deleteAllRoleUserByUser(Long userId, String companyName) {
        roleUserRepository.deleteAllRoleUserByUser(userId, companyName);
    }

    public void deleteAllRoleUserByRoleId(Long roleId, String companyName) {
        roleUserRepository.deleteAllRoleUserByRoleId(roleId, companyName);
    }

    public void deleteRole(Long roleId, String companyName) {
        rolesRepository.deleteRoleByUserId(roleId, companyName);
    }
}