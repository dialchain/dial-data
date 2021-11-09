package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.domain.BtcCustomMemoryBlockStore;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import com.plooh.adssi.dial.data.domain.BtcTransactionStore;
import com.plooh.adssi.dial.data.domain.enums.BitcoinNetworkEnum;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener;
import org.bitcoinj.core.listeners.NewBestBlockListener;
import org.bitcoinj.core.listeners.OnTransactionBroadcastListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@AllArgsConstructor
@Slf4j
@Configuration
public class BitcoinSPVProvider {

    private final BitcoinConfig bitcoinConfig;
    private final BtcTransactionStore btcTransactionStore;

    @Bean
    @Primary
    public BlockStore blockStore() {
        BlockStore blockStore = new BtcCustomMemoryBlockStore(getParams());
        return blockStore;
    }

    @Bean
    @Primary
    public PeerGroup peerGroup() throws BlockStoreException, UnknownHostException {
        log.info("===== Connecting to Bitcoin Node =====");
        final NetworkParameters params = getParams();
        BlockChain chain = new BlockChain(params, blockStore());
        PeerGroup peerGroup = new PeerGroup(params, chain);
        if (bitcoinConfig.getFastCatchupTimeDays() != null){
            peerGroup.setFastCatchupTimeSecs(getFastCatchupTimeSecs());
        }

        if (bitcoinConfig.getLocalhost()) {
            peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        } else {
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        }

        peerGroup.addOnTransactionBroadcastListener(Threading.USER_THREAD, new OnTransactionBroadcastListener() {
            @Override
            public void onTransaction(Peer peer, Transaction t) {
                log.info("Broadcasted transaction hash is {}", t.getTxId());
                var btcTransaction = findOrCreateBtcTransaction(t);
                btcTransaction.addPeer(peer.getAddress().toString());
            }
        });

        peerGroup.addBlocksDownloadedEventListener(Threading.USER_THREAD, new BlocksDownloadedEventListener() {
            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                block.getTransactions()
                    .forEach(tx -> {
                        var btcTransaction = findOrCreateBtcTransaction(tx);
                        btcTransaction.addPeer(peer.getAddress().toString());
                        btcTransaction.addBlockId(block.getHash().toString());
                    });
            }
        });

        chain.addNewBestBlockListener(Threading.USER_THREAD, new NewBestBlockListener(){
            @Override
            public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
                block.getHeader().getTransactions()
                .forEach( tx -> {
                    var btcTransaction = findOrCreateBtcTransaction(tx);
                    btcTransaction.addBestBlockId(block.getHeader().getHash().toString());
                });
            }
        });

        return peerGroup;
    }

    private BtcTransaction findOrCreateBtcTransaction(Transaction tx){
        var btcTransaction = btcTransactionStore.find(tx.getTxId().toString());
        if (btcTransaction == null){
            // If tx does not exist create tx with the corresponding best block
            btcTransaction = BtcTransaction.builder()
                .id(tx.getTxId().toString())
                .transactionBytes(getTransactionBytes(tx))
                .build();
            btcTransactionStore.save(btcTransaction);
        }
        return btcTransaction;
    }

    private byte[] getTransactionBytes(Transaction tx) {
        byte[] transactions = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            tx.bitcoinSerialize(bos);
            transactions = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
        return transactions;
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
            .orElseGet(() -> BitcoinNetworkEnum.MAIN);
        return bitcoinNetworkEnum.get();
    }

}
