package com.provesoft.resource.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class DocumentType {

    public DocumentType (String companyName,
                         String name,
                         String description,
                         String documentPrefix,
                         Integer maxNumberOfDigits,
                         Long startingNumber) {

        this.companyName = companyName;
        this.name = name;
        this.description = description;
        this.documentPrefix = documentPrefix;
        this.maxNumberOfDigits = maxNumberOfDigits;
        this.startingNumber = startingNumber;
        this.currentSuffix = startingNumber;
    }

    public DocumentType() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    Long id;

    String companyName;
    String name;
    String description;
    String documentPrefix;
    Integer maxNumberOfDigits;
    Long startingNumber;

    Long currentSuffix;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDocumentPrefix() {
        return documentPrefix;
    }

    public void setDocumentPrefix(String documentPrefix) {
        this.documentPrefix = documentPrefix;
    }

    public Integer getMaxNumberOfDigits() {
        return maxNumberOfDigits;
    }

    public void setMaxNumberOfDigits(Integer maxNumberOfDigits) {
        this.maxNumberOfDigits = maxNumberOfDigits;
    }

    public Long getStartingNumber() {
        return startingNumber;
    }

    public void setStartingNumber(Long startingNumber) {
        this.startingNumber = startingNumber;
    }

    public Long getCurrentSuffix() {
        return currentSuffix;
    }

    public void setCurrentSuffix(Long currentSuffix) {
        this.currentSuffix = currentSuffix;
    }
}
