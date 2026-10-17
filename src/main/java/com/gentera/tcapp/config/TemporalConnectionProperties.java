package com.gentera.tcapp.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "temporal.connection")
public class TemporalConnectionProperties {
    private String host;

    private int port;

    private boolean enableHttps = false;

    private boolean defaultConfiguration = true;
}
