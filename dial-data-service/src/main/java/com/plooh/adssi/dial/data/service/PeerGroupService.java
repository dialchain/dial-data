package com.plooh.adssi.dial.data.service;

import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PeerGroupService {

    private final PeerGroup peerGroup;

    public void submitTransaction(byte[] transactionBytes) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(), transactionBytes));
    }

}
