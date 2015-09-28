package com.provesoft.resource.validators;

import com.provesoft.resource.entity.Organizations;
import com.provesoft.resource.entity.Roles;
import com.provesoft.resource.entity.UserDetails;
import com.provesoft.resource.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;

public final class AdminFormValidation {

    private AdminFormValidation() {}

    @Autowired
    UsersService usersService;

    public static Boolean validateNewUser(UserDetails userDetails) {

        // Upper and lower case letters
        String alphaRegex = "^[a-zA-Z]+$";

        // Email format
        String emailRegex = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";

        String firstname = userDetails.getFirstName();
        String lastname = userDetails.getLastName();
        String title = userDetails.getTitle();
        String email = userDetails.getEmail();
        Organizations org = userDetails.getPrimaryOrganization();

        if (firstname == null) {
            return false;
        }
        else if (   (firstname.length() > 254) ||
                    (firstname.length() == 0) ||
                    (firstname.equals("")) ||
                    (!firstname.matches(alphaRegex))
                ) {
            return false;
        }

        if (lastname == null) {
            return false;
        }
        else if (   (lastname.length() > 254) ||
                    (lastname.length() == 0) ||
                    (lastname.equals("")) ||
                    (!lastname.matches(alphaRegex))
                ) {
            return false;
        }

        if (title == null) {
            return false;
        }
        else if (   (title.length() > 254) ||
                    (title.length() == 0) ||
                    (title.equals(""))
                ) {
            return false;
        }

        if (email == null) {
            return false;
        }
        else if (
                (email.length() ==0) ||
                (email.length() > 254) ||
                (email.equals("")) ||
                (!email.matches(emailRegex))
            ) {
            return false;
        }

        if (org == null) {
            return false;
        }

        return true;
    }


    public static Boolean validateNewOrganization(Organizations organization) {

        String name = organization.getName();
        String description = organization.getDescription();

        if (name == null) {
            return false;
        }
        else if (
                    (name.length() == 0) ||
                    (name.length() > 254) ||
                    (name.equals(""))
                ) {
            return false;
        }

        if (description == null) {
            return false;
        }
        else if (
                    (description.length() == 0) ||
                    (description.length() > 1023) ||
                    (description.equals(""))
                ) {
            return false;
        }

        return true;
    }


    public static Boolean validateNewRole(Roles role) {

        String name = role.getName();
        String description = role.getDescription();

        if (name == null) {
            return false;
        }
        else if (
                (name.length() == 0) ||
                        (name.length() > 254) ||
                        (name.equals(""))
                ) {
            return false;
        }

        if (description == null) {
            return false;
        }
        else if (
                (description.length() == 0) ||
                        (description.length() > 1023) ||
                        (description.equals(""))
                ) {
            return false;
        }

        return true;
    }
}
