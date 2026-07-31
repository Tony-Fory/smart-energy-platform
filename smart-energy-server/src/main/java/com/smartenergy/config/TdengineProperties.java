package com.smartenergy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tdengine")
public class TdengineProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
