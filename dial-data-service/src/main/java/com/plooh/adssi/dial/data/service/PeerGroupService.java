package com.plooh.adssi.dial.data.service;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PeerGroupService {

    private final PeerGroup peerGroup;

    public void broadcastTransaction(byte[] txBytes) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(),
                txBytes));
    }

}
