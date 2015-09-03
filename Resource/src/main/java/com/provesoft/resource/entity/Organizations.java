package com.provesoft.resource.entity;

import javax.persistence.*;

/**
 * Entity holds information for a specific organization.
 */
@Entity
public class Organizations {

    public Organizations (String name, String companyName, String description) {

        this.name = name;
        this.companyName = companyName;
        this.description = description;
    }

    public Organizations() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long organizationId;

    private String name;
    private String companyName;
    private String description;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
