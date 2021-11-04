package com.plooh.adssi.dial.data.domain;

public interface BtcTransactionStore {

    void save(BtcTransaction transaction);

    BtcTransaction find(String transactionId);

}
