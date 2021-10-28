package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.domain.BitcoinNetworkEnum;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
@Slf4j
@Configuration
public class BitcoinSPVProvider {

    private final BitcoinConfig bitcoinConfig;

    @Bean
    @Primary
    public PeerGroup peerGroup() throws BlockStoreException, UnknownHostException {
        log.info("===== Connecting to Bitcoin Node =====");
        final NetworkParameters params = getParams();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        if (bitcoinConfig.getFastCatchupTimeDays() != null){
            peerGroup.setFastCatchupTimeSecs(getFastCatchupTimeSecs());
        }

        if (bitcoinConfig.getLocalhost()) {
            peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        } else {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        }
        return peerGroup;
    }

    private Long getFastCatchupTimeSecs(){
        LocalDate localDate = LocalDate.now().minusDays(bitcoinConfig.getFastCatchupTimeDays());
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long timeInMillis = instant.toEpochMilli() / 1000;
        return timeInMillis;
    }

    private NetworkParameters getParams(){
        // Figure out which network we should connect to.
        BitcoinNetworkEnum bitcoinNetworkEnum = BitcoinNetworkEnum.fromValue(bitcoinConfig.getNetwork())
            .orElse(BitcoinNetworkEnum.MAIN);
        return bitcoinNetworkEnum.get();
    }

}
