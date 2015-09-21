package com.provesoft.gateway.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class NewUserTokens {

    public NewUserTokens(String email, String token) {
        this.email = email;
        this.token = token;
        this.genDate = new Date();
    }

    public NewUserTokens() {
        // Default constructor
    }

    @Id
    private String email;

    private String token;

    private Date genDate;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getGenDate() {
        return genDate;
    }

    public void setGenDate(Date genDate) {
        this.genDate = genDate;
    }
}
