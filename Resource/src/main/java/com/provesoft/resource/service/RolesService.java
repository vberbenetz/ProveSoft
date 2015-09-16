package com.provesoft.resource.service;

import com.provesoft.resource.entity.RolePermissions;
import com.provesoft.resource.entity.RoleUser;
import com.provesoft.resource.entity.Roles;
import com.provesoft.resource.repository.RolePermissionsRepository;
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

    @Autowired
    RolePermissionsRepository rolePermissionsRepository;


    public List<Roles> findByCompanyName(String companyName) {
        return rolesRepository.findByCompanyName(companyName);
    }

    public Roles findByCompanyNameAndRoleId(String companyName, Long roleId) {
        return rolesRepository.findByCompanyNameAndRoleId(companyName, roleId);
    }

    public List<Roles> findByCompanyNameAndRoleIdList(String companyName, List<Long> roleIds) {
        return rolesRepository.findByCompanyNameAndRoleIdIn(companyName, roleIds);
    }

    /**
     * Method checks to see if this organization is referred elsewhere in the app
     * @param companyName
     * @param role
     * @return Boolean
     */
    public Boolean roleInUse(String companyName, Roles role) {
        if ( roleUserRepository.countByKeyCompanyNameAndKeyRoleId(companyName, role.getRoleId()) > 0 ) {
            return true;
        }
        else {
            return false;
        }
    }

    public Roles saveRole(Roles role) {
        return rolesRepository.saveAndFlush(role);
    }

    public void deleteRole(Roles role) {
        rolesRepository.delete(role);
        rolesRepository.flush();
    }

    /**
     * Method deletes a Role and it's RolePermissions if it is not referenced anywhere else
     * @param companyName
     * @param role
     * @return Boolean
     */
    public Boolean removeRole(String companyName, Roles role) {
        if (!roleInUse(companyName, role)) {
            rolesRepository.delete(role);
            rolePermissionsRepository.deleteByRoleId(role.getRoleId());
            rolesRepository.flush();
            return true;
        }
        else {
            return false;
        }
    }


    /* ---------------------- RolePermissions -------------------- */

    public List<RolePermissions> findRolePermissionsByRoleId(Long roleId) {
        return rolePermissionsRepository.findByKeyRoleId(roleId);
    }

    public List<RolePermissions> saveRolePermissions(List<RolePermissions> rolePermissions) {
        List<RolePermissions> savedRolePermissions = rolePermissionsRepository.save(rolePermissions);
        rolePermissionsRepository.flush();
        return savedRolePermissions;
    }

    public void deleteAllRolePermissionsByRoleId(Long roleId) {
        rolePermissionsRepository.deleteByRoleId(roleId);
        rolePermissionsRepository.flush();
    }


    /* ---------------------- RoleUser -------------------- */

    public List<RoleUser> findRoleUserByUserId(Long userId) {
        return roleUserRepository.findByKeyUserId(userId);
    }

    public List<RoleUser> findRoleUserByRoleId(Long roleId) {
        return roleUserRepository.findByKeyRoleId(roleId);
    }

    public List<RoleUser> addRoleUsers(List<RoleUser> roleUsers) {
        List<RoleUser> ru = roleUserRepository.save(roleUsers);
        roleUserRepository.flush();
        return ru;
    }

    public void deleteRoleUser(String companyName, Long userId, Long roleId) {
        roleUserRepository.deleteRoleUser(companyName, userId, roleId);
    }

    public void deleteAllRoleUserByUserId(String companyName, Long userId) {
        roleUserRepository.deleteAllByUserId(companyName, userId);
    }

}
