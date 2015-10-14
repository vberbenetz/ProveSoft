package com.provesoft.gateway.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BetaKeys {

    public BetaKeys(String email, String betaKey) {
        this.email = email;
        this.betaKey = betaKey;
    }

    public BetaKeys() {}

    @Id
    String email;

    String betaKey;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBetaKey() {
        return betaKey;
    }

    public void setBetaKey(String betaKey) {
        this.betaKey = betaKey;
    }
}
