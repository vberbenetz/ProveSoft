package com.provesoft.resource.entity;


import javax.persistence.*;

@Entity
public class Roles {

    public Roles (
            String name,
            String companyName,
            String description) {

        this.name = name;
        this.companyName = companyName;
        this.description = description;
    }

    public Roles() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long roleId;

    private String companyName;
    private String name;
    private String description;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
