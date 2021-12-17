package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.exception.AddressNotFound;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.NotChainHead;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import com.plooh.adssi.dial.data.repository.BtcBlockStore;
import com.plooh.adssi.dial.data.service.BtcBlockService;
import com.plooh.adssi.dial.data.service.PeerGroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final BtcBlockStore btcBlockService;
    private final PeerGroupService peerGroupService;

    @Override
    public ResponseEntity<Void> broadcastTransaction(BtcTransactionDto btcTransactionDto) {
        peerGroupService.broadcastTransaction(btcTransactionDto);
        return ResponseEntity.accepted().build();
    }

    @Override
    public byte[] getChainhead() {
        return btcBlockService.getChainhead().orElseThrow(() -> new NotChainHead());
    }

    @Override
    public byte[] getBlockHeadersForBlockHash(String blockHash) {
        return btcBlockService.getBlockHeadersByBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
    }

    @Override
    public byte[] getBlockHashForTxId(String txId) {
        return btcBlockService.getBlockHashByTxId(Utils.HEX.decode(txId))
                .orElseThrow(() -> new TransactionNotFound(txId));
    }

    @Override
    public byte[] getTxsForAddress(String address) {

        return btcBlockService.getTxsForAddress(Address.fromString(btcBlockService.getParams(), address).getHash())
                .orElseThrow(() -> new AddressNotFound(address));
    }

    @Override
    public byte[] getBlockForBlockHash(String blockHash) {
        return btcBlockService.getBlockForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
    }

    @Override
    public byte[] getTxIdsForBlockHash(String blockHash) {
        return btcBlockService.getTxIdsForBlockId(Utils.HEX.decode(blockHash)).orElseThrow(() -> Bnew BlockNotFound(blockHash));
    }

}
