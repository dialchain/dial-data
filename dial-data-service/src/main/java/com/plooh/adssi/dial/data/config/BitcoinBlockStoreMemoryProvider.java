package com.plooh.adssi.dial.data.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.store.MemoryFullPrunedBlockStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@RequiredArgsConstructor
@Slf4j
@Configuration
@Profile("!postgres")
public class BitcoinBlockStoreMemoryProvider {

    private final BitcoinProperties bitcoinConfig;

    @Bean
    @Primary
    public FullPrunedBlockStore blockStore() {
        log.info("=== Using the IN Memory Blockstore ===");
        FullPrunedBlockStore blockStore = new MemoryFullPrunedBlockStore(bitcoinConfig.getParams(), bitcoinConfig.getFullStoreDepth());
        return blockStore;
    }

}
