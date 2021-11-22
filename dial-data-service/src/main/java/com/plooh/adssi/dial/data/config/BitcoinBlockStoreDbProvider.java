package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.dto.JDBCUrl;
import java.net.MalformedURLException;
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

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public FullPrunedBlockStore blockStore() throws BlockStoreException, MalformedURLException {
        log.info("=== Using the Postgres Blockstore ===");
        var jdbcUrl = JDBCUrl.parse(url).orElseThrow(() -> new MalformedURLException(String.format("The connection [%s] url to the postgres database is malformed")));
        FullPrunedBlockStore blockStore = new PostgresFullPrunedBlockStore(bitcoinConfig.getParams(), bitcoinConfig.getFullStoreDepth(),
            jdbcUrl.getHostname(), jdbcUrl.getDatabase(), username, password);
        return blockStore;
    }

}
