package com.plooh.adssi.dial.data.repository;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.plooh.adssi.dial.data.domain.BtcAddressTx;
import com.plooh.adssi.dial.data.domain.BtcBlockTxIds;
import com.plooh.adssi.dial.data.domain.BtcBytesList;
import com.plooh.adssi.dial.data.domain.Bytes;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BtcBlockStore {

    private final DialBtcBlockStore dialBtcBlockStore;
    private final BlockStore blockStore;

    public Optional<byte[]> getChainhead() {
        try {
            return Optional.ofNullable(blockStore.getChainHead())
                    .map(this::serializeStoredBlock);
        } catch (BlockStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<byte[]> getBlockHeadersForBlockHash(byte[] blockHash) {
        return readStoredBlock(blockHash)
                .map(storedBlock -> serializeStoredBlock(storedBlock));
    }

    public Optional<byte[]> getBlockHashForTxId(byte[] txId) {
        return dialBtcBlockStore.get(txId);
    }

    public Optional<byte[]> getTxsForAddress(byte[] address) {
        return dialBtcBlockStore.get(address);
    }

    public Optional<byte[]> getBlockForBlockHash(byte[] blockHash) {
        return dialBtcBlockStore.get(blockHash);
    }

    public Optional<byte[]> getTxIdsForBlockHash(byte[] blockHash) {
        return dialBtcBlockStore.get(blockHash)
                .map(blockByte -> new Block(dialBtcBlockStore.getParams(), blockByte,
                        dialBtcBlockStore.getParams().getDefaultSerializer(),
                        blockByte.length))
                .map(block -> new BtcBlockTxIds(getTxIds(block)))
                .map(btcBlockTxIds -> btcBlockTxIds.toBytes());
    }

    private List<Sha256Hash> getTxIds(Block block) {
        return block.getTransactions() == null ? Collections.emptyList()
                : block.getTransactions().stream().map(tx -> tx.getTxId()).collect(Collectors.toList());
    }

    /**
     * Storing a block.
     * - We store one entry for the block bytes.
     * - We extract and map each address to the list of affected transactions/block
     * - We store one entry for eachtransaction to refer to the hosting block
     * 
     * @param block
     */
    public void storeBtcBlock(Block block) {
        byte[] blockHashBytes = block.getHash().getBytes();
        // Store the block bytes
        dialBtcBlockStore.put(blockHashBytes, block.unsafeBitcoinSerialize());
        if (block.getTransactions() != null) {
            // Store the addresses frominput and output
            handleTransactionInputs(block.getHash(), block.getTransactions());
            handleTransactionOutputs(block.getHash(), block.getTransactions());
            // Map each txId to the blockId
            block.getTransactions().stream()
                    .forEach(tx -> dialBtcBlockStore.put(tx.getTxId().getBytes(), blockHashBytes));
        }
    }

    private void handleTransactionOutputs(final Sha256Hash blockHash, final List<Transaction> transactions) {
        if (transactions == null) {
            return;
        }
        List<TransactionOutput> transactionOutputs = transactions.stream()
                .map(Transaction::getOutputs)
                .findAny()
                .orElseGet(() -> List.of());

        Map<Address, List<BtcAddressTx>> btcAddresses = transactionOutputs.stream()
                .map(txOutput -> addressTx(txOutput, blockHash))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(BtcAddressTx::getAddress));

        storeAddresses(btcAddresses);
    }

    private void storeAddresses(Map<Address, List<BtcAddressTx>> btcAddresses) {
        Map<Address, BtcBytesList> btcAddressesBytes = btcAddresses.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> toBtcBytesList(e.getValue())));

        btcAddressesBytes.entrySet().forEach(e -> {
            byte[] addressHash = e.getKey().getHash();
            BtcBytesList newEntries = e.getValue();
            BtcBytesList bl = dialBtcBlockStore.get(addressHash).map(b -> BtcBytesList.fromBytes(b).merge(newEntries))
                    .orElseGet(() -> newEntries);
            dialBtcBlockStore.put(addressHash, bl.sort().toBytes());
        });
    }

    private BtcBytesList toBtcBytesList(List<BtcAddressTx> list) {
        return BtcBytesList
                .wrap(list.stream().map(BtcAddressTx::toBytes).map(b -> Bytes.wrap(b)).collect(Collectors.toList()));
    }

    private BtcAddressTx addressTx(TransactionOutput txOutput, final Sha256Hash blockHash) {
        try {
            Address _toAddress = _toAddress(txOutput);
            if (_toAddress != null) {
                return new BtcAddressTx(false, txOutput.getParentTransaction().getTxId(), blockHash,
                        _toAddress);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;

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

    private void handleTransactionInputs(final Sha256Hash blockHash, List<Transaction> transactions) {
        if (transactions == null) {
            return;
        }

        List<TransactionInput> transactionInputs = transactions.stream()
                .map(Transaction::getInputs)
                .findAny()
                .orElseGet(() -> List.of());
        Map<Address, List<BtcAddressTx>> btcAddresses = transactionInputs.stream()
                .filter(txInput -> !txInput.isCoinBase())
                .map(txInput -> findAddress(txInput).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(BtcAddressTx::getAddress));
        storeAddresses(btcAddresses);
    }

    private Optional<BtcAddressTx> findAddress(TransactionInput input) {
        return getBlockHashForTxId(input.getOutpoint().getHash().getBytes())
                .map(h -> relevantOutput(Sha256Hash.wrap(h), input)).get();
    }

    private Optional<BtcAddressTx> relevantOutput(Sha256Hash blockHash, TransactionInput input) {
        return dialBtcBlockStore.get(blockHash.getBytes())
                .map(b -> new Block(dialBtcBlockStore.getParams(), b,
                        dialBtcBlockStore.getParams().getDefaultSerializer(),
                        b.length))
                .map(block -> matchingTx(block, input.getOutpoint().getHash()).orElse(null))
                .map(fromTx -> findRelevantOutput(fromTx, input).orElse(null))
                .map(out -> new BtcAddressTx(true, input.getOutpoint().getHash(),
                        blockHash, _toAddress(out)));
    }

    private Optional<TransactionOutput> findRelevantOutput(Transaction fromTx, TransactionInput input) {
        List<TransactionOutput> relevantOutputs = fromTx.getOutputs();
        for (TransactionOutput ro : relevantOutputs) {
            try {
                input.verify(ro);
                return Optional.of(ro);
            } catch (Exception e) {
                // ignore.
            }
        }
        return Optional.empty();
    }

    private Optional<Transaction> matchingTx(Block block, Sha256Hash txId) {
        return block.getTransactions() == null ? Optional.empty()
                : block.getTransactions().stream().filter(tx -> tx.getTxId().equals(txId)).findAny();
    }

    private Optional<StoredBlock> readStoredBlock(byte[] bockHash) {
        try {
            return Optional.ofNullable(blockStore.get(Sha256Hash.wrap(bockHash)));
        } catch (BlockStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] serializeStoredBlock(StoredBlock storedBlock) {
        ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
        storedBlock.serializeCompact(buffer);
        return buffer.array();
    }

    public NetworkParameters getParams() {
        return dialBtcBlockStore.getParams();
    }

}
