package com.provesoft.resource.entity;

import javax.persistence.*;

@Entity
public class UserDetails {

    public UserDetails(String companyName,
                       String userName,
                       String firstName,
                       String lastName,
                       String email,
                       String title,
                       Long primaryOrgId)
    {
        this.companyName = companyName;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.title = title;
        this.primaryOrgId = primaryOrgId;
    }

    public UserDetails() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    private Long userId;

    private String companyName;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String title;
    private Long primaryOrgId;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Long getPrimaryOrgId() {
        return primaryOrgId;
    }

    public void setPrimaryOrgId(Long primaryOrgId) {
        this.primaryOrgId = primaryOrgId;
    }

}
