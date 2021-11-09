package com.plooh.adssi.dial.data.domain;

import java.util.stream.Stream;
import org.bitcoinj.core.StoredBlock;

public interface BtcTransactionStore {

    void save(BtcTransaction transaction);

    BtcTransaction find(String transactionId);

    Stream<StoredBlock> findBlocks();

}
