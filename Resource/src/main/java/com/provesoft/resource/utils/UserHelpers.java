package com.provesoft.resource.utils;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * Class contains helper methods for User related items
 */
public final class UserHelpers {

    private UserHelpers() {
        // Private constructor
    }

    /**
     * Method checks if user is of type SUPER_ADMIN
     * @param auth Authentication object
     * @return Boolean on whether user is a Super_Admin
     */
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

    /**
     * Method retrieves user's company name.
     * @param auth Authentication object
     * @return String of company name
     */
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
