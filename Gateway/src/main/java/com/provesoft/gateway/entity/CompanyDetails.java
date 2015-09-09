package com.provesoft.gateway.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CompanyDetails {

    public CompanyDetails(String companyName, Long numberOfLicenses, String plan) {
        this.companyName = companyName;
        this.numberOfLicenses = numberOfLicenses;
        this.plan = plan;
    }

    public CompanyDetails() {
        // Default Constructor
    }

    @Id
    String companyName;

    private Long numberOfLicenses;

    private String plan;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getNumberOfLicenses() {
        return numberOfLicenses;
    }

    public void setNumberOfLicenses(Long numberOfLicenses) {
        this.numberOfLicenses = numberOfLicenses;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
