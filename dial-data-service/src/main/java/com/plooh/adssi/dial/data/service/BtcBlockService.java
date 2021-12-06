package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.domain.*;
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

    public BtcTransaction findOrCreateBtcTransaction(Transaction tx, String blockId) {
        BtcTransaction btcTransaction = null;
        try {
            btcTransaction = btcBlockStore.findByTxId(tx.getTxId().toString());
        } catch (TransactionNotFound e) {
            // If tx does not exist create tx with the corresponding best block
            btcTransaction = BtcTransaction.builder()
                    .txId(tx.getTxId().toString())
                    .build();
        }

        if (blockId != null) {
            btcTransaction.addBlockId(blockId);
        }
        return saveTransaction(btcTransaction);
    }

    public BtcTransaction saveTransaction(BtcTransaction btcTransaction) {
        return btcBlockStore.save(btcTransaction);
    }

    public BtcCheckTransactionResponse checkTransaction(String txId) {
        var btcTransaction = btcBlockStore.findByTxId(txId);
        var response = new BtcCheckTransactionResponse()
                .reportingBlocks(Optional.ofNullable(btcTransaction.getBlockIds()).map(Set::size).orElse(0));
        return response;
    }

    public BtcFullBlockHeadersResponse findBlockByTransactionId(String txId) {
        var response = btcBlockStore.findBlockHeadersByTxId(txId)
                .map(this::mapToBtcFullBlockHeadersResponse)
                .orElse(null);
        return response;
    }

    public BtcBlockHeadersResponse getBlocksByHeight(int startHeight, int endHeight) {
        List<BtcBlockHeaderDto> btcBlockHeaders = null;
        if (endHeight >= startHeight) {
            // TODO: configurable mac height. Let us use 144 (6 * 24)
            endHeight = Math.min(endHeight, startHeight + 144);
            btcBlockHeaders = btcBlockStore.getBlocksByHeight(startHeight, endHeight).stream()
                    .map(this::mapToBtcBlockHeaderDto)
                    .collect(Collectors.toList());
        } else {
            btcBlockHeaders = Collections.emptyList();
        }
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
                .blockHash(btcBlockHeader.getBlockId())
                .height(btcBlockHeader.getHeight())
                .chainWork(btcBlockHeader.getChainWork())
                .time(btcBlockHeader.getTime())
                .prevBlockHash(btcBlockHeader.getPrevBlockHash())
                .txIds(new ArrayList<>(btcBlockHeader.getTxIds()));
    }

    private BtcFullBlockHeadersResponse mapToBtcFullBlockHeadersResponse(BtcBlockHeader btcBlockHeader) {
        BtcBlock btcBlock = btcBlockStore.getBlockById(btcBlockHeader.getBlockId());
        return new BtcFullBlockHeadersResponse()
                .blockHash(btcBlockHeader.getBlockId())
                .height(btcBlockHeader.getHeight())
                .chainWork(btcBlockHeader.getChainWork())
                .time(btcBlockHeader.getTime())
                .prevBlockHash(btcBlockHeader.getPrevBlockHash())
                .txIds(new ArrayList<>(btcBlockHeader.getTxIds()))
                .blockBytes(btcBlock.getBlockBytes());
    }

    public BtcBlockDto getBlock(String blockId) {
        BtcBlock btcBlock = btcBlockStore.getBlockById(blockId);
        return new BtcBlockDto().blockBytes(btcBlock.getBlockBytes());
    }

    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height, Integer chainWork) {
        return btcBlockStore.findOrCreateBtcBlock(block, height, chainWork);
    }

    public BtcTransactionsResponse getTransactionsByAddress(String address) {
        List<BtcAddress> transactions = btcBlockStore.getTransactionsByAddress(address);
        return new BtcTransactionsResponse()
                .txIds(transactions.stream().map(BtcAddress::getTxId).collect(Collectors.toList()));
    }

}
