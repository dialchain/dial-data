package com.plooh.adssi.dial.data.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.store.PostgresFullPrunedBlockStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@RequiredArgsConstructor
@Slf4j
@Configuration
@Profile("postgres")
public class BitcoinBlockStoreDbProvider {

    private final BitcoinProperties bitcoinConfig;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${dial.data.db.hostname}")
    private String dbHostname;

    @Value("${dial.data.db.dbName}")
    private String dbName;

    @Bean
    @Primary
    public FullPrunedBlockStore blockStore() throws BlockStoreException {
        log.info("=== Using the Postgres Blockstore ===");
        FullPrunedBlockStore blockStore = new PostgresFullPrunedBlockStore(bitcoinConfig.getParams(), bitcoinConfig.getFullStoreDepth(),
                    dbHostname, dbName, username, password);
        return blockStore;
    }

}
