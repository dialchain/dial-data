package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.service.BtcBlockService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.utils.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
@Slf4j
@Configuration
public class BitcoinSPVProvider {

    private final BitcoinProperties bitcoinProperties;
    private final FullPrunedBlockStore blockStore;
    private final BtcBlockService btcBlockService;

    @Bean
    @Primary
    public PeerGroup peerGroup() throws BlockStoreException, UnknownHostException {
        log.info("=== Connecting to Bitcoin Node ===");
        final NetworkParameters params = bitcoinProperties.getParams();
        FullPrunedBlockChain chain = new FullPrunedBlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);

        if (bitcoinProperties.getLocalhost()) {
            peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        } else {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        }

        peerGroup.addOnTransactionBroadcastListener(Threading.USER_THREAD, (peer, t) -> {
            log.debug("=== Broadcasted transaction hash is {} ===", t.getTxId());
            btcBlockService.findOrCreateBtcTransaction(t, null);
        });

        peerGroup.addBlocksDownloadedEventListener(Threading.USER_THREAD, (peer, block, filteredBlock, blocksLeft) -> {
            Optional.ofNullable(block.getTransactions()).ifPresent( txs -> {
                txs.forEach(tx -> btcBlockService.findOrCreateBtcTransaction(tx, block.getHash().toString()));
                });
            btcBlockService.findOrCreateBtcBlock(block, null);
        });

        chain.addNewBestBlockListener(Threading.USER_THREAD,
            block -> btcBlockService.findOrCreateBtcBlock(block.getHeader(), block.getHeight()));

        return peerGroup;
    }
}
