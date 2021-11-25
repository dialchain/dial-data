package com.plooh.adssi.dial.data.store;

import com.plooh.adssi.dial.data.domain.BtcBlockHeader;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import java.util.List;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.StoredBlock;

public interface BtcBlockStore {

    BtcTransaction save(BtcTransaction transaction);

    BtcTransaction find(String transactionId);

    List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight);

    List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity);

    StoredBlock getBlock(String blockId);

    int getBlockDepth(int blockHeight);

    BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height);

}
