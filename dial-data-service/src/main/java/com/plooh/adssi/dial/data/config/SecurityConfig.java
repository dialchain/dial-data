package com.plooh.adssi.dial.data.config;

import com.nimbusds.jose.JWSAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Data
@Validated
@Configuration
@ConfigurationProperties("dial.data.messaging.security.signature")
public class SecurityConfig {

    @NotNull
    private Duration ttl;

    @NotNull
    private Boolean enabled;

    @NotNull
    private JWSAlgorithm algorithm;
}
