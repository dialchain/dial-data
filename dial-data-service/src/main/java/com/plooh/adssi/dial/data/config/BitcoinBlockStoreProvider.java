package com.plooh.adssi.dial.data.config;

import java.io.File;
import java.net.MalformedURLException;

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

    private final BitcoinProperties bitcoinConfig;

    @Bean
    @Primary
    public BlockStore blockStore() throws BlockStoreException {
        // TODO externalize
        String usrHome = System.getProperty("user.home");
        File usrHomeFile = new File(usrHome);
        log.info("=== Using the LevelDB Blockstore - Directory: {} ===", usrHomeFile.getAbsolutePath());
        File daataDir = new File(usrHome, ".bitcoinj");
        BlockStore blockStore = new LevelDBBlockStore(Context.getOrCreate(bitcoinConfig.getParams()), daataDir);
        return blockStore;
    }

}
