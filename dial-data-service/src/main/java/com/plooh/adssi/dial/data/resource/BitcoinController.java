package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.bitcoin.model.BtcBlockResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcCheckTransactionResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcFindBlockResponse;
import com.plooh.adssi.dial.data.service.BtcTransactionService;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final BtcTransactionService btcTransactionService;

    @Override
    public ResponseEntity<Void> submitTransaction(byte[] transactionBytes, Optional<String> xPayment) {
        btcTransactionService.submitTransaction(transactionBytes, xPayment);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BtcCheckTransactionResponse> checkTransaction(String transactionId, Optional<String> xPayment) {
        var response = btcTransactionService.checkTransaction(transactionId, xPayment);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BtcFindBlockResponse> getBlockByTransactionId(String transactionId, Optional<String> xPayment) {
        var response = btcTransactionService.findBlockByTransactionId(transactionId, xPayment);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BtcBlockResponse> listBlocks(OffsetDateTime dateTime, Optional<String> xPayment) {
        var response = btcTransactionService.listBlocks(dateTime, xPayment);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<byte[]> getBlock(String blockId, Optional<String> xPayment) {
        var response = btcTransactionService.getBlock(blockId, xPayment);
        return ResponseEntity.ok(response);
    }

}
