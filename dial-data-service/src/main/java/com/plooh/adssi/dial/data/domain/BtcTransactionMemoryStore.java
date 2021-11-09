package com.plooh.adssi.dial.data.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.store.BlockStore;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BtcTransactionMemoryStore implements BtcTransactionStore {

    private Map<String, BtcTransaction> transactionMap = new HashMap<>();

    private final BlockStore blockStore;

    @Override
    public synchronized void save(BtcTransaction transaction) {
        transactionMap.put(transaction.getId(), transaction);
    }

    @Override
    public synchronized BtcTransaction find(String transactionId) {
        return transactionMap.get(transactionId);
    }

    @Override
    public Stream<StoredBlock> findBlocks() {
        return ((BtcCustomMemoryBlockStore) blockStore).getBlocks();
    }

}
