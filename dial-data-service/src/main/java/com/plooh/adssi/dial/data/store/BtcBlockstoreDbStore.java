package com.plooh.adssi.dial.data.store;

import com.plooh.adssi.dial.data.domain.*;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Profile("postgres")
public class BtcBlockstoreDbStore implements BtcBlockStore {

    private final BtcTransactionRepository btcTransactionRepository;
    private final BtcBlockHeaderRepository btcBlockHeaderRepository;
    private final BtcBlockRepository btcBlockRepository;
    private final BtcAddressRepository btcAddressRepository;

    @Transactional
    @Override
    public BtcTransaction save(BtcTransaction transaction) {
        return btcTransactionRepository.save(transaction);
    }

    @Override
    public BtcTransaction findByTxId(String transactionId) {
        return btcTransactionRepository.findByTxId(transactionId)
            .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    @Override
    public Optional<BtcBlockHeader> findBlockHeadersByTxId(String txId) {
        return btcBlockHeaderRepository.findByTxId(txId);
    }

    @Override
    public List<BtcAddress> getTransactionsByAddress(String address) {
        return btcAddressRepository.findByAddress(address);
    }

    @Override
    public List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight) {
        return btcBlockHeaderRepository.findByHeightBetween(startHeight, endHeight);
    }

    @Override
    public List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity) {
        return btcBlockHeaderRepository.findByTimeGreaterThanEqual(startTime, PageRequest.of(0, quantity.intValue()));
    }

    @Override
    public BtcBlock getBlockById(String blockId) {
        return btcBlockRepository.findById(blockId)
            .orElseThrow(() -> new BlockNotFound(blockId));
    }

    @Override
    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height, Integer chainWork) {
        BtcBlockHeader btcBlockHeader = btcBlockHeaderRepository.findById(block.getHashAsString())
            .orElseGet(() -> BtcBlockHeader.builder()
                .blockId(block.getHashAsString())
                .build());

        btcBlockHeader.setTime(Math.toIntExact(block.getTimeSeconds()));

        if ( block.getPrevBlockHash() ==  null){
            btcBlockHeader.setPrevBlockHash(block.getPrevBlockHash().toString());
        }
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

        btcBlockHeader = btcBlockHeaderRepository.save(btcBlockHeader);

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
            .collect(Collectors.toSet());
        if (!btcAddresses.isEmpty()){
            btcAddressRepository.saveAll(btcAddresses);
        }
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
            .collect(Collectors.toSet());

        if (!btcAddresses.isEmpty()){
            btcAddressRepository.saveAll(btcAddresses);
        }
    }

    private void findOrCreateBtcBlockPayload(Block block) {
        if (block.getTransactions() == null){
            return;
        }

        BtcBlock btcB = btcBlockRepository.findById(block.getHashAsString())
            .orElseGet(() -> BtcBlock.builder()
                .blockId(block.getHashAsString())
                .build());
        btcB.setBlockBytes(block.unsafeBitcoinSerialize());
        btcBlockRepository.save(btcB);
    }

}
