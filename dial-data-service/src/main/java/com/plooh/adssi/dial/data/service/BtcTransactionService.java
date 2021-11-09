package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.BtcBlock;
import com.plooh.adssi.dial.data.bitcoin.model.BtcBlockResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcCheckTransactionResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcFindBlockResponse;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import com.plooh.adssi.dial.data.domain.BtcTransactionStore;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BtcTransactionService {

    private final PeerGroup peerGroup;
    private final BlockStore blockStore;
    private final BtcTransactionStore btcTransactionStore;

    public BtcTransaction findTransaction(String transactionId) {
        return Optional.ofNullable(btcTransactionStore.find(transactionId))
            .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    public void submitTransaction(byte[] transactionBytes, Optional<String> xPayment) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(), transactionBytes));
    }

    public BtcCheckTransactionResponse checkTransaction(String transactionId, Optional<String> xPayment) {
        var btcTransaction = findTransaction(transactionId);
        var response = new BtcCheckTransactionResponse()
                .reportingPeers(btcTransaction.getPeerIds().size())
                .reportingBlocks(btcTransaction.getBlockIds().size());
        return response;
    }

    public BtcFindBlockResponse findBlockByTransactionId(String transactionId, Optional<String> xPayment) {
        var btcTransaction = findTransaction(transactionId);
        var response = new BtcFindBlockResponse();
        if (btcTransaction.getBlockIds() != null){
            // If block id of block in the best chain available
            List<String> blockIds = new ArrayList<>(btcTransaction.getBlockIds());
            for (int i = 0; i < blockIds.size(); i++) {
                var storedBlock = getStoredBlock(blockIds.get(i));
                if (storedBlock.isPresent()){
                    response = new BtcFindBlockResponse()
                        .blockId(storedBlock.get().getHeader().getHashAsString())
                        .depth(getBlockDepth(storedBlock.get()));
                    break;
                }
            }
        }
        return response;
    }

    public BtcBlockResponse listBlocks(OffsetDateTime dateTime, Optional<String> xPayment) {
        Date fromDate = new Date(dateTime.toInstant().toEpochMilli());
        var blocks = btcTransactionStore.findBlocks()
            .filter(storedBlock -> storedBlock.getHeader().getTime().after(fromDate))
            .map(storedBlock -> {
                var txs= storedBlock.getHeader().getTransactions().stream()
                    .map(transaction -> transaction.getTxId().toString())
                    .collect(Collectors.toList());
                return new BtcBlock().hash(storedBlock.getHeader().getHashAsString()).transactionIds(txs);
            })
            .collect(Collectors.toList());
        return new BtcBlockResponse().blocks(blocks);
    }

    public byte[] getBlock(String blockId, Optional<String> xPayment) {
        var block = btcTransactionStore.findBlocks()
            .filter(storedBlock -> storedBlock.getHeader().getHashAsString().equals(blockId))
            .findFirst()
            .map(StoredBlock::getHeader)
            .map(Block::unsafeBitcoinSerialize)
            .orElseThrow(() -> new BlockNotFound(blockId));
        return block;
    }

    private Optional<StoredBlock> getStoredBlock(String blockHash){
        final Sha256Hash hash = Sha256Hash.wrap(blockHash);
        StoredBlock storedBlock = null;
        try {
            storedBlock = blockStore.get(hash);
        } catch (BlockStoreException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(storedBlock);
    }

    private int getBlockDepth(StoredBlock storedBlock){
        try {
            return blockStore.getChainHead().getHeight() - storedBlock.getHeight() ;
        } catch (BlockStoreException e) {
            log.warn(e.getMessage());
            return 0;
        }
    }

}
