package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.repository.DialBtcBlockStore;

import java.nio.file.Paths;
import org.bitcoinj.core.Context;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.LevelDBBlockStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class BitcoinBlockStoreProvider {

    private final BitcoinProperties bitcoinProperties;

    @Bean
    @Primary
    public BlockStore blockStore() throws BlockStoreException {
        var path = Paths.get(bitcoinProperties.getBlockstoreDir());
        log.info("=== Using the LevelDB Blockstore - Directory: {} ===", path.toAbsolutePath());
        var blockStore = new LevelDBBlockStore(Context.getOrCreate(bitcoinProperties.getParams()), path.toFile());
        return blockStore;
    }

    @Bean
    @Primary
    public DialBtcBlockStore dialBtcBlockStore() throws BlockStoreException {
        var path = Paths.get(bitcoinProperties.getDialblockstoreDir());
        log.info("=== Using the LevelDB DialBtcBlockStore - Directory: {} ===", path.toAbsolutePath());
        var dialBtcBlockStore = new DialBtcBlockStore(Context.getOrCreate(bitcoinProperties.getParams()), path.toFile());
        return dialBtcBlockStore;
    }

}
