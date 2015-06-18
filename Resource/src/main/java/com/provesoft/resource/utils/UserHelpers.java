package com.provesoft.resource.utils;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public final class UserHelpers {

    private UserHelpers() {
        // Private constructor
    }

    public static Boolean isSuperAdmin(Authentication auth) {

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        for (GrantedAuthority ga : authorities) {

            String authority = ga.getAuthority();

            if (authority.equals("ROLE_SUPER_ADMIN")) {
                return true;
            }
        }

        return false;
    }

    public static String getCompany (Authentication auth) {

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        for (GrantedAuthority ga : authorities) {

            String authority = ga.getAuthority();

            if (authority.startsWith("__")) {
                return(authority.split("__")[1]);
            }
        }

        return null;
    }
}
