package com.plooh.adssi.dial.data.service;

import com.plooh.adssi.dial.data.bitcoin.model.BtcTransactionDto;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PeerGroupService {

    private final PeerGroup peerGroup;

    public void submitTransaction(BtcTransactionDto btcTransactionDto) {
        peerGroup.broadcastTransaction(new Transaction(peerGroup.getVersionMessage().getParams(), btcTransactionDto.getTransactionBytes()));
    }

}
