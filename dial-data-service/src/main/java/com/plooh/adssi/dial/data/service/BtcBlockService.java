package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.domain.*;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
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

    private final BtcBlockStore btcTransactionStore;

    public BtcTransaction findOrCreateBtcTransaction(Transaction tx, String blockId){
        BtcTransaction btcTransaction = null;
        try {
            btcTransaction = btcTransactionStore.find(tx.getTxId().toString());
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
        return btcTransactionStore.save(btcTransaction);
    }

    public BtcCheckTransactionResponse checkTransaction(String txId) {
        var btcTransaction = btcTransactionStore.find(txId);
        var response = new BtcCheckTransactionResponse()
                .reportingBlocks(btcTransaction.getBlockIds().size());
        return response;
    }

    public BtcFindBlockResponse findBlockByTransactionId(String txId) {
        var btcTransaction = btcTransactionStore.find(txId);
        var response = new BtcFindBlockResponse();
        if (btcTransaction.getBlockIds() != null){
            // If block id of block in the best chain available
            List<String> blockIds = new ArrayList<>(btcTransaction.getBlockIds());
            for (int i = 0; i < blockIds.size(); i++) {
                try {
                    StoredBlock storedBlock = btcTransactionStore.getBlock(blockIds.get(i));
                    response = new BtcFindBlockResponse()
                        .blockId(storedBlock.getHeader().getHashAsString())
                        .depth(btcTransactionStore.getBlockDepth(storedBlock.getHeight()));
                    break;
                } catch (BlockNotFound ignored){}
            }
        }
        return response;
    }

    public BtcBlockHeadersResponse getBlocksByHeight(int startHeight, int endHeight) {
        var btcBlockHeaders = btcTransactionStore.getBlocksByHeight(startHeight, endHeight).stream()
            .map(this::mapToBtcBlockHeaderDto)
            .collect(Collectors.toList());
        return new BtcBlockHeadersResponse().blocks(btcBlockHeaders);
    }

    public BtcBlockHeadersResponse getBlocksByTime(Integer startTime, Integer quantity) {
        var btcBlockHeaders = btcTransactionStore.getBlocksByTime(startTime, quantity).stream()
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
            .txIds(new ArrayList<>(btcBlockHeader.getTxIds()));
    }

    public BtcBlockDto getBlock(String blockId) {
        StoredBlock storedBlock = btcTransactionStore.getBlock(blockId);
        return new BtcBlockDto().blockAsBytes(storedBlock.getHeader().unsafeBitcoinSerialize());
    }

    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height) {
        return btcTransactionStore.findOrCreateBtcBlock(block, height);
    }

}
