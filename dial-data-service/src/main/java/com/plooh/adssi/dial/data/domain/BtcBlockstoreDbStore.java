package com.plooh.adssi.dial.data.domain;

import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Profile("postgres")
public class BtcBlockstoreDbStore implements BtcBlockStore {

    private final BtcTransactionRepository btcTransactionRepository;
    private final BtcBlockHeaderRepository btcBlockHeaderRepository;
    private final BtcBlockRepository btcBlockRepository;
    private final BlockStore blockStore;

    @Transactional
    @Override
    public BtcTransaction save(BtcTransaction transaction) {
        return btcTransactionRepository.save(transaction);
    }

    @Override
    public BtcTransaction find(String transactionId) {
        return btcTransactionRepository.findByTxId(transactionId)
            .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    @Override
    public List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight) {
        return btcBlockHeaderRepository.findByHeightBetween(startHeight, endHeight);
    }

    @Override
    public List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity) {
        return btcBlockHeaderRepository.findByTimeGreaterThanEqual(startTime, PageRequest.of(0, quantity.intValue()));
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

    @Override
    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height) {
        BtcBlockHeader btcBlockHeader = btcBlockHeaderRepository.findById(block.getHashAsString())
            .orElseGet(() -> BtcBlockHeader.builder()
                .blockId(block.getHashAsString())
                .build());

        btcBlockHeader.setTime(Math.toIntExact(block.getTimeSeconds()));
        btcBlockHeader.setPrevBlockHash(block.getPrevBlockHash().toString());
        if ( height != null ){
            btcBlockHeader.setHeight(height);
        }
        if ( block.getTransactions() != null ){
            btcBlockHeader.setTxIds(block.getTransactions().stream()
                .map(Transaction::getTxId)
                .map(Sha256Hash::toString)
                .collect(Collectors.toSet()));
        }

        btcBlockHeader = btcBlockHeaderRepository.save(btcBlockHeader);

        findOrCreateBtcBlockPayload(block);
        return btcBlockHeader;
    }

    private void findOrCreateBtcBlockPayload(Block block) {
        if (block.getTransactions() == null){
            return;
        }

        BtcBlock btcB = btcBlockRepository.findById(block.getHashAsString())
            .orElseGet(() -> BtcBlock.builder()
                .blockId(block.getHashAsString())
                .build());
        btcB.setBlockBytes(block.unsafeBitcoinSerialize());
        btcBlockRepository.save(btcB);
    }

}
