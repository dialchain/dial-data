package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.BtcTransactionRequest;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PeerGroupService {

    private final PeerGroup peerGroup;

    public void broadcastTransaction(BtcTransactionRequest btcTransactionRequest) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(),
                btcTransactionRequest.getTransactionBytes()));
    }

}
