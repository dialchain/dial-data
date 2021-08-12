package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.store.redis.RedisStoreConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackageClasses = RedisStoreConfig.class)
@Profile("!test")
public class StoreConfig {
}