package com.plooh.adssi.dial.data.repository;

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
import org.bitcoinj.script.ScriptPattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BtcBlockStore {

    private final BtcTransactionRepository btcTransactionRepository;
    private final BtcBlockHeaderRepository btcBlockHeaderRepository;
    private final BtcBlockRepository btcBlockRepository;
    private final BtcAddressRepository btcAddressRepository;

    @Transactional
    public BtcTransaction save(BtcTransaction transaction) {
        return btcTransactionRepository.save(transaction);
    }

    public BtcTransaction findByTxId(String transactionId) {
        return btcTransactionRepository.findByTxId(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    public Optional<BtcBlockHeader> findBlockHeadersByTxId(String txId) {
        return btcBlockHeaderRepository.findByTxId(txId);
    }

    public List<BtcAddress> getTransactionsByAddress(String address) {
        return btcAddressRepository.findByAddress(address);
    }

    public List<BtcBlockHeader> getBlocksByHeight(int startHeight, int endHeight) {
        return btcBlockHeaderRepository.findByHeightBetween(startHeight, endHeight);
    }

    public List<BtcBlockHeader> getBlocksByTime(Integer startTime, Integer quantity) {
        return btcBlockHeaderRepository.findByTimeGreaterThanEqual(startTime, PageRequest.of(0, quantity.intValue()));
    }

    public BtcBlock getBlockById(String blockId) {
        return btcBlockRepository.findById(blockId)
                .orElseThrow(() -> new BlockNotFound(blockId));
    }

    public BtcBlockHeader findOrCreateBtcBlock(Block block, Integer height, Integer chainWork) {
        BtcBlockHeader btcBlockHeader = btcBlockHeaderRepository.findById(block.getHashAsString())
                .orElseGet(() -> BtcBlockHeader.builder()
                        .blockId(block.getHashAsString())
                        .build());

        btcBlockHeader.setTime(Math.toIntExact(block.getTimeSeconds()));

        if (block.getPrevBlockHash() != null) {
            btcBlockHeader.setPrevBlockHash(block.getPrevBlockHash().toString());
        }
        if (height != null) {
            btcBlockHeader.setHeight(height);
        }
        if (chainWork != null) {
            btcBlockHeader.setChainWork(chainWork);
        }

        if (block.getTransactions() != null) {
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
        if (transactions == null) {
            return;
        }

        List<TransactionOutput> transactionOutputs = transactions.stream()
                .map(Transaction::getOutputs)
                .findAny()
                .orElseGet(() -> List.of());

        var btcAddresses = transactionOutputs.stream()
                .map(txOutput -> {
                    try {
                        Address _toAddress = _toAddress(txOutput);
                        if (_toAddress != null) {
                            return BtcAddress.builder()
                                    .address(_toAddress.toString())
                                    .blockId(blockHash)
                                    .txId(txOutput.getParentTransaction().getTxId().toString())
                                    .input(false)
                                    .build();
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!btcAddresses.isEmpty()) {
            btcAddressRepository.saveAll(btcAddresses);
        }
    }

    private Address _toAddress(TransactionOutput output) {
        var script = output.getScriptPubKey();
        Address toAddress = null;
        if (ScriptPattern.isP2PK(script)) {
            toAddress = script.getToAddress(output.getParams(), true);
        } else if (ScriptPattern.isP2PKH(script) || ScriptPattern.isP2WH(script) || ScriptPattern.isP2SH(script)) {
            toAddress = script.getToAddress(output.getParams());
        }
        return toAddress;
    }

    private void handleTransactionInputs(final String blockHash, List<Transaction> transactions) {
        if (transactions == null) {
            return;
        }

        List<TransactionInput> transactionInputs = transactions.stream()
                .map(Transaction::getInputs)
                .findAny()
                .orElseGet(() -> List.of());

        var btcAddresses = transactionInputs.stream()
                .filter(txInput -> !txInput.isCoinBase())
                .map(txInput -> {
                    try {
                        Address address = findAddress(txInput);
                        if (address != null) {
                            var btcAddress = BtcAddress.builder()
                                    .address(address.toString())
                                    .blockId(blockHash)
                                    .txId(txInput.getParentTransaction().getTxId().toString())
                                    .input(true)
                                    .build();
                            return btcAddress;

                        }
                    } catch (Exception e) {
                        log.trace(e.getMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!btcAddresses.isEmpty()) {
            btcAddressRepository.saveAll(btcAddresses);
        }
    }

    private Address findAddress(TransactionInput input) {
        TransactionOutPoint outpoint = input.getOutpoint();
        // final fromTxHash = outpoint.hash;
        String outputTxId = outpoint.getHash().toString();
        BtcBlockHeader blockHeadersByTxId = findBlockHeadersByTxId(outputTxId).orElse(null);
        TransactionOutput relevantOutput = null;
        if (blockHeadersByTxId != null) {
            BtcBlock blockById = getBlockById(blockHeadersByTxId.getBlockId());
            Block block = new Block(input.getParams(), blockById.getBlockBytes(),
                    input.getParams().getDefaultSerializer(), 0);
            Transaction fromTx = block.getTransactions().stream()
                    .filter(tx -> tx.getTxId().toString().equals(outputTxId)).findFirst().orElse(null);
            List<TransactionOutput> relevantOutputs = fromTx.getOutputs();
            for (TransactionOutput ro : relevantOutputs) {
                try {
                    input.verify(ro);
                    relevantOutput = ro;
                    break;
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
        if (relevantOutput != null) {
            return _toAddress(relevantOutput);
        }
        return null;
    }

    private void findOrCreateBtcBlockPayload(Block block) {
        if (block.getTransactions() == null) {
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
