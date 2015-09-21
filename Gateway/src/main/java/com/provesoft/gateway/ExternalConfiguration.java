package com.provesoft.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ExternalConfiguration {

    private String absoluteUrl;

    private String host;
    private Integer port;
    private String user;
    private String password;

    @Autowired
    public ExternalConfiguration(@Value("${url.abs}") String absoluteUrl,
                                 @Value("${mail.host}") String host,
                                 @Value("${mail.port}") Integer port,
                                 @Value("${mail.user}") String user,
                                 @Value("${mail.password}") String password) {


        this.absoluteUrl = absoluteUrl;

        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
