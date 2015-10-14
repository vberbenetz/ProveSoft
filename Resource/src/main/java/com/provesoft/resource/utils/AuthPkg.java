package com.provesoft.resource.utils;


import org.springframework.security.core.Authentication;

/**
 * Class is used to return basic authentication information about a User.
 */
public class AuthPkg {

    private AuthPkg() {}

    public AuthPkg(Authentication auth) {

        try {
            this.companyName = UserHelpers.getCompany(auth);
            this.userName = auth.getName();
            this.isSuperAdmin = UserHelpers.isSuperAdmin(auth);
            this.isSystemAdmin = UserHelpers.isSystemAdmin(auth);
        }
        catch (Exception ex) {
            this.companyName = null;
            this.userName = null;
            this.isSuperAdmin = null;
            this.isSystemAdmin = null;
        }
    }

    private String companyName;
    private String userName;
    private Boolean isSuperAdmin;
    private Boolean isSystemAdmin;

    public String getCompanyName() {
        return companyName;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean getIsSuperAdmin() {
        return isSuperAdmin;
    }

    public Boolean getIsSystemAdmin() {
        return isSystemAdmin;
    }
}
