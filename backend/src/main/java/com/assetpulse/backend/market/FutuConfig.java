package com.assetpulse.backend.market;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.futu")
public class FutuConfig {
    private String host;
    private int port;
}