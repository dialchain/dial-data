package com.plooh.adssi.dial.data.domain;

import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!postgres")
public class BtcBlockstoreMemoryStore implements BtcBlockStore {

    private Map<String, BtcTransaction> transactionMap = new HashMap<>();
    private Map<String, BtcBlock> blockMap = new HashMap<>();
    private Map<String, BtcBlockHeader> blockHeaderMap = new HashMap<>();

    private final BlockStore blockStore;

    @Override
    public synchronized BtcTransaction save(BtcTransaction transaction) {
        transactionMap.put(transaction.getTxId(), transaction);
        return transaction;
    }

    @Override
    public synchronized BtcTransaction find(String transactionId) {
        return Optional.ofNullable(transactionMap.get(transactionId))
            .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    @Override
    public List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight) {
        return blockHeaderMap.values().stream()
            .filter(Objects::nonNull)
            .filter( bH -> bH.getHeight() != null && bH.getHeight() >= startHeight && bH.getHeight() <= endHeight )
            .collect(Collectors.toList());
    }

    @Override
    public List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity) {
        return blockHeaderMap.values().stream()
            .filter(Objects::nonNull)
            .filter( bH -> bH.getTime() != null && bH.getTime() >= startTime)
            .limit(quantity)
            .collect(Collectors.toList());
    }

    @Override
    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height) {
        BtcBlockHeader btcB = blockHeaderMap.get(block.getHashAsString());
        if( btcB == null ){
            btcB = BtcBlockHeader.builder()
                .blockId(block.getHashAsString())
                .build();
        }
        btcB.setTime(new Long(block.getTimeSeconds()).intValue());
        btcB.setPrevBlockHash(block.getPrevBlockHash().toString());
        if ( height != null ){
            btcB.setHeight(height);
        }
        if ( block.getTransactions() != null ){
            btcB.setTxIds(block.getTransactions().stream()
                .map(Transaction::getTxId)
                .map(Sha256Hash::toString)
                .collect(Collectors.toSet()));
        }

        BtcBlockHeader btcBlockHeader = blockHeaderMap.put(block.getHashAsString(), btcB);

        findOrCreateBtcBlockPayload(block);
        return btcBlockHeader;
    }

    private void findOrCreateBtcBlockPayload(Block block) {
        if (block.getTransactions() == null){
            return;
        }

        BtcBlock btcB = blockMap.get(block.getHashAsString());
        if( btcB == null ){
            btcB = BtcBlock.builder()
                .blockId(block.getHashAsString())
                .build();
        }
        btcB.setBlockBytes(block.unsafeBitcoinSerialize());
        blockMap.put(block.getHashAsString(), btcB);
    }


    @Override
    public StoredBlock getBlock(String blockId) {
        try {
            return Optional.ofNullable(blockStore.get(Sha256Hash.wrap(blockId)))
                .orElseThrow(() -> new BlockNotFound(blockId));
        } catch (BlockStoreException e) {
            throw new BlockNotFound(blockId);
        }
    }

    @Override
    public int getBlockDepth(int blockHeight) {
        try {
            return blockStore.getChainHead().getHeight() - blockHeight;
        } catch (BlockStoreException e) {
            log.warn(e.getMessage());
            return 0;
        }
    }

}
