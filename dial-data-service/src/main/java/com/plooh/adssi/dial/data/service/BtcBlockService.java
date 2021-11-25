package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.domain.*;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import com.plooh.adssi.dial.data.store.BtcBlockStore;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BtcBlockService {

    private final BtcBlockStore btcBlockStore;

    public BtcTransaction findOrCreateBtcTransaction(Transaction tx, String blockId){
        BtcTransaction btcTransaction = null;
        try {
            btcTransaction = btcBlockStore.find(tx.getTxId().toString());
        } catch (TransactionNotFound e){
            // If tx does not exist create tx with the corresponding best block
            btcTransaction = BtcTransaction.builder()
                .txId(tx.getTxId().toString())
                .build();
        }

        if ( blockId != null) {
            btcTransaction.addBlockId(blockId);
        }
        return saveTransaction(btcTransaction);
    }

    public BtcTransaction saveTransaction(BtcTransaction btcTransaction) {
        return btcBlockStore.save(btcTransaction);
    }

    public BtcCheckTransactionResponse checkTransaction(String txId) {
        var btcTransaction = btcBlockStore.find(txId);
        var response = new BtcCheckTransactionResponse()
                .reportingBlocks(btcTransaction.getBlockIds() == null ? null : btcTransaction.getBlockIds().size());
        return response;
    }

    public BtcFindBlockResponse findBlockByTransactionId(String txId) {
        var btcTransaction = btcBlockStore.find(txId);
        var response = new BtcFindBlockResponse();
        if (btcTransaction.getBlockIds() != null){
            // If block id of block in the best chain available
            List<String> blockIds = new ArrayList<>(btcTransaction.getBlockIds());
            for (int i = 0; i < blockIds.size(); i++) {
                try {
                    StoredBlock storedBlock = btcBlockStore.getBlock(blockIds.get(i));
                    response = new BtcFindBlockResponse()
                        .blockId(storedBlock.getHeader().getHashAsString())
                        .depth(btcBlockStore.getBlockDepth(storedBlock.getHeight()));
                    break;
                } catch (BlockNotFound ignored){}
            }
        }
        return response;
    }

    public BtcBlockHeadersResponse getBlocksByHeight(int startHeight, int endHeight) {
        var btcBlockHeaders = btcBlockStore.getBlocksByHeight(startHeight, endHeight).stream()
            .map(this::mapToBtcBlockHeaderDto)
            .collect(Collectors.toList());
        return new BtcBlockHeadersResponse().blocks(btcBlockHeaders);
    }

    public BtcBlockHeadersResponse getBlocksByTime(Integer startTime, Integer quantity) {
        var btcBlockHeaders = btcBlockStore.getBlocksByTime(startTime, quantity).stream()
            .map(this::mapToBtcBlockHeaderDto)
            .collect(Collectors.toList());
        return new BtcBlockHeadersResponse().blocks(btcBlockHeaders);
    }

    private BtcBlockHeaderDto mapToBtcBlockHeaderDto(BtcBlockHeader btcBlockHeader) {
        return new BtcBlockHeaderDto()
            .blockId(btcBlockHeader.getBlockId())
            .height(btcBlockHeader.getHeight())
            .time(btcBlockHeader.getTime())
            .prevBlockHash(btcBlockHeader.getPrevBlockHash())
            .txIds( btcBlockHeader.getTxIds() ==  null ? null : new ArrayList<>(btcBlockHeader.getTxIds()));
    }

    public BtcBlockDto getBlock(String blockId) {
        StoredBlock storedBlock = btcBlockStore.getBlock(blockId);
        return new BtcBlockDto().blockAsBytes(storedBlock.getHeader().unsafeBitcoinSerialize());
    }

    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height) {
        return btcBlockStore.findOrCreateBtcBlock(block, height);
    }

}
