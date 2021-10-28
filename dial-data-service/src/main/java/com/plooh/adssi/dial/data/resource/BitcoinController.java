package com.plooh.adssi.dial.data.resource;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.bitcoin.model.BtcCheckTransactionResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcFindBlockResponse;
import com.plooh.adssi.dial.data.bitcoin.model.BtcListBlockResponse;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final PeerGroup peerGroup;

    @Override
    public ResponseEntity<BtcCheckTransactionResponse> checkTransaction(String transactionId, Optional<String> xPayment) {
        return BitcoinApi.super.checkTransaction(transactionId, xPayment);
    }

    @Override
    public ResponseEntity<BtcFindBlockResponse> findBlock(String transactionId, Optional<String> xPayment) {
        return BitcoinApi.super.findBlock(transactionId, xPayment);
    }

    @Override
    public ResponseEntity<BtcListBlockResponse> listBlocks(OffsetDateTime dateTime, Optional<String> xPayment) {
        return BitcoinApi.super.listBlocks(dateTime, xPayment);
    }

    @Override
    public ResponseEntity<Void> submitTransaction(String transactionId, byte[] body, Optional<String> xPaymentJson) {
        // TODO: Where do we need the transactionId?
        Transaction tx = new Transaction(peerGroup.getVersionMessage().getParams(), body);
        TransactionBroadcast txBroadcast = peerGroup.broadcastTransaction(tx);
        // TODO: listen to event?
        return ResponseEntity.ok().build();
    }

}
