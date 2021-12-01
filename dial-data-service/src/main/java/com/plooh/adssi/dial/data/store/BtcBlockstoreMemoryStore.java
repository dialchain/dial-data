package com.plooh.adssi.dial.data.store;

import com.plooh.adssi.dial.data.domain.BtcAddress;
import com.plooh.adssi.dial.data.domain.BtcBlock;
import com.plooh.adssi.dial.data.domain.BtcBlockHeader;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!postgres")
public class BtcBlockstoreMemoryStore implements BtcBlockStore {

    private Map<String, BtcTransaction> transactionMap = new HashMap<>();
    private Map<String, List<BtcAddress>> addressMap = new HashMap<>();
    private Map<String, BtcBlock> blockMap = new HashMap<>();
    private Map<String, BtcBlockHeader> blockHeaderMap = new HashMap<>();

    @Override
    public synchronized BtcTransaction save(BtcTransaction transaction) {
        transactionMap.put(transaction.getTxId(), transaction);
        return transaction;
    }

    public synchronized BtcAddress saveAddress(BtcAddress btcAddress) {
        var list = addressMap.get(btcAddress.getAddress());
        if (list == null){
            list = new ArrayList<>();
            addressMap.put(btcAddress.getAddress(), list);
        }
        list.add(btcAddress);
        return btcAddress;
    }

    public synchronized List<BtcAddress> saveAddresses(List<BtcAddress> btcAddresses) {
        btcAddresses.stream().forEach(this::saveAddress);
        return btcAddresses;
    }

    @Override
    public synchronized BtcTransaction findByTxId(String transactionId) {
        return Optional.ofNullable(transactionMap.get(transactionId))
            .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    @Override
    public Optional<BtcBlockHeader> findBlockHeadersByTxId(String txId) {
        return blockHeaderMap.values().stream()
            .filter(Objects::nonNull)
            .filter( bH -> bH.getTxIds().contains(txId))
            .findFirst();
    }

    @Override
    public List<BtcAddress> getTransactionsByAddress(String address) {
        return addressMap.get(address);
    }

    @Override
    public List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight) {
        return blockHeaderMap.values().stream()
            .filter(Objects::nonNull)
            .filter( bH -> bH.getHeight() != null && bH.getHeight() >= startHeight && bH.getHeight() <= endHeight )
            .collect(Collectors.toList());
    }

    @Override
    public List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity) {
        return blockHeaderMap.values().stream()
            .filter(Objects::nonNull)
            .filter( bH -> bH.getTime() != null && bH.getTime() >= startTime)
            .limit(quantity)
            .collect(Collectors.toList());
    }

    @Override
    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height, Integer chainWork) {
        BtcBlockHeader btcBlockHeader = Optional.ofNullable(blockHeaderMap.get(block.getHashAsString()))
            .orElseGet(() -> BtcBlockHeader.builder()
                .blockId(block.getHashAsString())
                .build());
        btcBlockHeader.setTime(Math.toIntExact(block.getTimeSeconds()));
        btcBlockHeader.setPrevBlockHash(block.getPrevBlockHash().toString());
        if ( height != null ){
            btcBlockHeader.setHeight(height);
        }
        if ( chainWork != null ){
            btcBlockHeader.setChainWork(chainWork);
        }
        if ( block.getTransactions() != null ){
            btcBlockHeader.setTxIds(block.getTransactions().stream()
                .map(Transaction::getTxId)
                .map(Sha256Hash::toString)
                .collect(Collectors.toSet()));

            handleTransactionInputs(block.getHashAsString(), block.getTransactions());
            handleTransactionOutputs(block.getHashAsString(), block.getTransactions());
        }
        blockHeaderMap.put(block.getHashAsString(), btcBlockHeader);

        findOrCreateBtcBlockPayload(block);
        return btcBlockHeader;
    }

    private void handleTransactionOutputs(final String blockHash, List<Transaction> transactions) {
        if (transactions == null){
            return;
        }

        List<TransactionOutput> transactionOutputs = transactions.stream()
            .map(Transaction::getOutputs)
            .findAny()
            .orElseGet(() -> List.of());

        var btcAddresses = transactionOutputs.stream()
            .map( txOutput -> {
                try {
                    var btcAddress = BtcAddress.builder()
                        .address(txOutput.getScriptPubKey().getToAddress(txOutput.getParams()).toString())
                        .blockId(blockHash)
                        .txId(txOutput.getParentTransaction().getTxId().toString())
                        .input(false)
                        .build();
                        return btcAddress;
                    } catch (Exception e){
                        log.warn(e.getMessage(), e);
                        return null;
                    }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        saveAddresses(btcAddresses);
    }

    private void handleTransactionInputs(final String blockHash, List<Transaction> transactions) {
        if (transactions == null){
            return;
        }

        List<TransactionInput> transactionInputs = transactions.stream()
            .map(Transaction::getInputs)
            .findAny()
            .orElseGet(() -> List.of());

        var btcAddresses = transactionInputs.stream()
            .filter(txInput -> !txInput.isCoinBase())
            .map( txInput -> {
                try {
                    var btcAddress = BtcAddress.builder()
                        .address(txInput.getScriptSig().getToAddress(txInput.getParams()).toString())
                        .blockId(blockHash)
                        .txId(txInput.getParentTransaction().getTxId().toString())
                        .input(true)
                        .build();
                    return btcAddress;
                } catch (Exception e){
                    log.warn(e.getMessage(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        saveAddresses(btcAddresses);
    }

    private void findOrCreateBtcBlockPayload(Block block) {
        if (block.getTransactions() == null){
            return;
        }

        BtcBlock btcBlock = Optional.ofNullable(blockMap.get(block.getHashAsString()))
            .orElseGet(() -> BtcBlock.builder()
                .blockId(block.getHashAsString())
                .build());
        btcBlock.setBlockBytes(block.unsafeBitcoinSerialize());
        blockMap.put(block.getHashAsString(), btcBlock);
    }

    @Override
    public BtcBlock getBlockById(String blockId) {
        return blockMap.get(blockId);
    }

}
