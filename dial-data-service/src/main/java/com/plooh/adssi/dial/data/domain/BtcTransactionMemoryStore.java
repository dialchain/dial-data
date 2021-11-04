package com.plooh.adssi.dial.data.domain;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BtcTransactionMemoryStore implements BtcTransactionStore {

    private Map<String, BtcTransaction> transactionMap = new HashMap<>();

    @Override
    public synchronized void save(BtcTransaction transaction) {
        transactionMap.put(transaction.getId(), transaction);
    }

    @Override
    public synchronized BtcTransaction find(String transactionId) {
        return transactionMap.get(transactionId);
    }

}
