package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.bitcoin.model.BtcBlock;
import com.plooh.adssi.dial.data.bitcoin.model.BtcBlockResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcCheckTransactionResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcFindBlockResponse;
import com.plooh.adssi.dial.data.domain.BtcCustomMemoryBlockStore;
import com.plooh.adssi.dial.data.domain.BtcTransactionMemoryStore;
import com.plooh.adssi.dial.data.domain.BtcTransactionStore;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final PeerGroup peerGroup;
    private final BlockStore blockStore;
    private final BtcTransactionStore btcTransactionStore;

    @Override
    public ResponseEntity<Void> submitTransaction(byte[] transactionBytes, Optional<String> xPayment) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(), transactionBytes));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BtcCheckTransactionResponse> checkTransaction(String transactionId, Optional<String> xPayment) {
        var btcTransaction = btcTransactionStore.find(transactionId);
        var response = new BtcCheckTransactionResponse();
        if (btcTransaction != null){
            response
                .reportingPeers(btcTransaction.getPeerIds().size())
                .reportingBlocks(btcTransaction.getBlockIds().size());
        }
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BtcFindBlockResponse> findBlock(String transactionId, Optional<String> xPayment) {
        var btcTransaction = btcTransactionStore.find(transactionId);
        var response = new BtcFindBlockResponse();
        if (btcTransaction != null && btcTransaction.getBlockIds() != null){
            // If block id of block in the best chain available
            List<String> blockIds = new ArrayList<>(btcTransaction.getBlockIds());
            for (int i = 0; i < blockIds.size(); i++) {
                response = getStoredBlock(blockIds.get(i)).map(sb -> new BtcFindBlockResponse()
                    .blockId(sb.getHeader().getHashAsString())
                    .depth(getBlockDepth(sb)))
                    .orElseGet(() -> new BtcFindBlockResponse());
            }
        }
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BtcBlockResponse> listBlocks(OffsetDateTime dateTime, Optional<String> xPayment) {
        Date fromDate = new Date(dateTime.toInstant().toEpochMilli());
        Stream<StoredBlock> storedBlocks =((BtcCustomMemoryBlockStore) blockStore).getBlocks();
        var blocks = storedBlocks
            .filter(storedBlock -> storedBlock.getHeader().getTime().after(fromDate))
            .map(storedBlock -> {
                var txs= storedBlock.getHeader().getTransactions().stream()
               .map(transaction -> transaction.getTxId().toString())
               .collect(Collectors.toList());
           return new BtcBlock().hash(storedBlock.getHeader().getHashAsString()).transactionIds(txs);
        })
        .collect(Collectors.toList());
        return ResponseEntity.ok(new BtcBlockResponse().blocks(blocks));
    }

    private Optional<StoredBlock> getStoredBlock(String blockHash){
        final Sha256Hash hash = Sha256Hash.wrap(blockHash);
        StoredBlock storedBlock = null;
        try {
            storedBlock = blockStore.get(hash);
        } catch (BlockStoreException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(storedBlock);
    }

    private int getBlockDepth(StoredBlock storedBlock){
        try {
            return blockStore.getChainHead().getHeight() - storedBlock.getHeight() ;
        } catch (BlockStoreException e) {
            log.warn(e.getMessage());
            return 0;
        }
    }

}
