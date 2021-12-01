package com.plooh.adssi.dial.data.store;

import com.plooh.adssi.dial.data.domain.BtcAddress;
import com.plooh.adssi.dial.data.domain.BtcBlock;
import com.plooh.adssi.dial.data.domain.BtcBlockHeader;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import java.util.List;
import java.util.Optional;
import org.bitcoinj.core.Block;

public interface BtcBlockStore {

    BtcTransaction save(BtcTransaction transaction);

    BtcTransaction findByTxId(String transactionId);

    List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight);

    List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity);

    BtcBlock getBlockById(String blockId);

    BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height, Integer chainWork);

    Optional<BtcBlockHeader> findBlockHeadersByTxId(String txId);

    List<BtcAddress> getTransactionsByAddress(String address);

}
