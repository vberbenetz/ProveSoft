package com.provesoft.resource.entity;

import javax.persistence.*;

/**
 * Used by GATEWAY for user role purposes
 */
@Entity
public class Authorities {

    public Authorities(String authority, Users user) {
        this.user = user;
        this.authority = authority;
    }

    public Authorities() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name = "username")
    private Users user;

    private String authority;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
