package com.provesoft.resource.utils;


import org.springframework.security.core.Authentication;

public class AuthPkg {

    private AuthPkg() {}

    public AuthPkg(Authentication auth) {

        try {
            this.companyName = UserHelpers.getCompany(auth);
            this.userName = auth.getName();
            this.isSuperAdmin = UserHelpers.isSuperAdmin(auth);
        }
        catch (Exception ex) {
            this.companyName = null;
            this.userName = null;
            this.isSuperAdmin = null;
        }
    }

    private String companyName;
    private String userName;
    private Boolean isSuperAdmin;

    public String getCompanyName() {
        return companyName;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean getIsSuperAdmin() {
        return isSuperAdmin;
    }
}
