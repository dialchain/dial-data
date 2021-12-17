package com.plooh.adssi.dial.data.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.plooh.adssi.dial.data.service.BtcBlockService;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@Configuration
public class BitcoinSPVProvider {

    private final BitcoinProperties bitcoinProperties;
    private final BlockStore blockStore;
    private final BtcBlockService btcBlockService;

    @Bean
    @Primary
    public PeerGroup peerGroup() throws UnknownHostException, BlockStoreException {
        log.info("=== Connecting to Bitcoin Node ===");
        final NetworkParameters params = bitcoinProperties.getParams();
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        Instant instant = Instant.parse("2021-12-15T00:00:00.00Z");
        peerGroup.setFastCatchupTimeSecs(instant.toEpochMilli() / 1000);

        if (bitcoinProperties.getLocalhost()) {
            peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        } else {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        }

        peerGroup.addBlocksDownloadedEventListener(Threading.USER_THREAD, (peer, block, filteredBlock, blocksLeft) -> {
            Optional.ofNullable(block.getTransactions()).ifPresent(txs -> {
                txs.forEach(tx -> btcBlockService.findOrCreateBtcTransaction(tx, block.getHash().toString()));
            });
            btcBlockService.findOrCreateBtcBlock(block, null, null);
        });

        chain.addNewBestBlockListener(Threading.USER_THREAD,
                block -> btcBlockService.findOrCreateBtcBlock(block.getHeader(), block.getHeight(),
                        block.getChainWork().intValue()));

        return peerGroup;
    }
}
