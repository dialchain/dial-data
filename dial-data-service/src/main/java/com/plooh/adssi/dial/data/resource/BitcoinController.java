package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.service.BtcBlockService;
import com.plooh.adssi.dial.data.service.PeerGroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final BtcBlockService btcBlockService;
    private final PeerGroupService peerGroupService;

    @Override
    public ResponseEntity<Void> submitTransaction(BtcTransactionDto btcTransactionDto) {
        peerGroupService.submitTransaction(btcTransactionDto);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BtcCheckTransactionResponse> checkTransaction(String txId) {
        return ResponseEntity.ok(btcBlockService.checkTransaction(txId));
    }

    @Override
    public ResponseEntity<BtcFindBlockResponse> getBlockByTransactionId(String txId) {
        return ResponseEntity.ok(btcBlockService.findBlockByTransactionId(txId));
    }

    @Override
    public ResponseEntity<BtcBlockHeadersResponse> getBlocksByHeight(Integer startHeight, Integer endHeight) {
        return ResponseEntity.ok(btcBlockService.getBlocksByHeight(startHeight, endHeight));
    }

    @Override
    public ResponseEntity<BtcBlockDto> getBlock(String blockId) {
        return ResponseEntity.ok(btcBlockService.getBlock(blockId));
    }

    @Override
    public ResponseEntity<BtcBlockHeadersResponse> getBlocksByTime(Integer startTime, Integer quantity) {
        return ResponseEntity.ok(btcBlockService.getBlocksByTime(startTime, quantity));
    }
}
