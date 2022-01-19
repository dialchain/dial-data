package com.plooh.adssi.dial.data.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NormalTime {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
