package com.plooh.adssi.dial.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@Configuration
@ConfigurationProperties("dial.data.bitcoin")
public class BitcoinConfig {

    @NotNull
    private String network;

    @NotNull
    private Boolean localhost;

    private Long fastCatchupTimeDays;

}
