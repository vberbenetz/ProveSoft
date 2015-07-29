package com.provesoft.gateway.entity;

import javax.persistence.*;

@Entity
public class UserDetails {

    public UserDetails(String companyName,
                       String firstName,
                       String lastName,
                       String email,
                       String title,
                       Organizations primaryOrganization)
    {
        this.companyName = companyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.title = title;
        this.primaryOrganization = primaryOrganization;
    }

    public UserDetails() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    private Long userId;

    private String companyName;
    private String email;
    private String firstName;
    private String lastName;
    private String title;

    @ManyToOne
    @JoinColumn
    private Organizations primaryOrganization;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Organizations getPrimaryOrganization() {
        return primaryOrganization;
    }

    public void setPrimaryOrganization(Organizations primaryOrganization) {
        this.primaryOrganization = primaryOrganization;
    }
}
