package com.plooh.adssi.dial.data.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.plooh.adssi.dial.data.repository.BtcBlockStore;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BtcBlockService {

    // private final BtcBlockStore btcBlockStore;

    // public String checkTransaction(String txId) {
    // return btcBlockStore.findBlockHeadersByTxId(Utils.HEX.decode(txId))
    // .map(blockHeader ->
    // Base64.getEncoder().encodeToString(blockHeader)).orElse(null);
    // }

    // public String findBlockByTransactionId(String txId) {
    // return checkTransaction(txId);
    // }

    // public List<String> getBlocksByHeight(int startHeight, int endHeight) {
    // List<String> btcBlockHeaders = new ArrayList<>();
    // if (endHeight >= startHeight) {
    // // TODO: configurable mac height. Let us use 144 (6 * 24)
    // endHeight = Math.min(endHeight, startHeight + 144);
    // for (int i = startHeight; i < startHeight + 1; i++) {
    // btcBlockHeaders.add(btcBlockStore.getBlocksByHeight(i)
    // .map(this::mapToBtcBlockHeaderDto).orElse(""));
    // }
    // }
    // return btcBlockHeaders;
    // }

    // // public BtcBlockHeadersResponse getBlocksByTime(Integer startTime, Integer
    // // quantity) {
    // // var btcBlockHeaders = btcBlockStore.getBlocksByTime(startTime,
    // // quantity).stream()
    // // .map(this::mapToBtcBlockHeaderDto)
    // // .collect(Collectors.toList());
    // // return new BtcBlockHeadersResponse().blocks(btcBlockHeaders);
    // // }

    // private String mapToBtcBlockHeaderDto(byte[] btcBlockHeader) {
    // return Base64.getEncoder().encodeToString(btcBlockHeader);
    // }

    // public String getBlock(String blockId) {
    // return btcBlockStore.getBlockById(Utils.HEX.decode(blockId))
    // .map(b -> Utils.HEX.encode(b)).orElse(null);
    // }

    // // public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height,
    // // Integer chainWork) {
    // // return btcBlockStore.findOrCreateBtcBlock(block, height, chainWork);
    // // }

    // // public BtcTransactionsResponse getTransactionsByAddress(String address) {
    // // List<BtcAddressTx> transactions =
    // // btcBlockStore.getTransactionsByAddress(address);
    // // return new BtcTransactionsResponse()
    // //
    // .txIds(transactions.stream().map(BtcAddressTx::getTxId).collect(Collectors.toList()));
    // // }

    // public void saveBtcTransaction(Transaction tx, Sh blockId) {
    // btcBlockStore.storeTransaction(tx, );
    // try {
    // btcTransaction = btcBlockStore.findByTxId(tx.getTxId().toString());
    // } catch (TransactionNotFound e) {
    // // If tx does not exist create tx with the corresponding best block
    // btcTransaction = BtcTransaction.builder()
    // .txId(tx.getTxId().toString())
    // .build();
    // }

    // if (blockId != null) {
    // btcTransaction.addBlockId(blockId);
    // }
    // return saveTransaction(btcTransaction);
    // }

    // public void processBlock(Block block) {
    // btcBlockStore.storeBlock(block);
    // }

    // public Object processStoredBlock(StoredBlock storedBlock) {
    // return null;
    // }
}
