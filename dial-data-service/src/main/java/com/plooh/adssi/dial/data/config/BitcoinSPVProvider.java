package com.plooh.adssi.dial.data.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

import com.plooh.adssi.dial.data.repository.BtcBlockStore;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
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
    private final BtcBlockStore btcBlockStore;

    @Bean
    @Primary
    public PeerGroup peerGroup() throws UnknownHostException, BlockStoreException {
        log.info("=== Connecting to Bitcoin Node ===");
        final NetworkParameters params = bitcoinProperties.getParams();
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);

        if (StringUtils.isNotBlank(bitcoinProperties.getFastCatchupTime())){
            try {
                Instant instant = Instant.parse(bitcoinProperties.getFastCatchupTime());
                peerGroup.setFastCatchupTimeSecs(instant.toEpochMilli() / 1000);
            } catch (Exception e){
                log.warn(String.format("FastCatchupTime property [%s] not properly configured. Please check...", bitcoinProperties.getFastCatchupTime()), e);
            }
        }

        if (bitcoinProperties.getLocalhost()) {
            peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        } else {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        }

        peerGroup.addOnTransactionBroadcastListener(Threading.USER_THREAD, (peer, transaction) -> {
            btcBlockStore.storeTransaction(transaction);
        });

        peerGroup.addBlocksDownloadedEventListener(Threading.USER_THREAD, (peer, block, filteredBlock, blocksLeft) -> {
            btcBlockStore.storeBtcBlock(block);
        });

        return peerGroup;
    }
}
