package com.provesoft.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ExternalConfiguration {

    private String absoluteUrl;

    @Autowired
    public ExternalConfiguration(@Value("${url.abs}") String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

}
