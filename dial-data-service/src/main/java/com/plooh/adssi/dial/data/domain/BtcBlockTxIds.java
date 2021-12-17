package com.plooh.adssi.dial.data.domain;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;

public class BtcBlockTxIds {

    private final List<Sha256Hash> txIds;

    public BtcBlockTxIds(List<Sha256Hash> txIds) {
        this.txIds = txIds == null ? Collections.emptyList() : txIds;
    }

    public List<Sha256Hash> getTxIds() {
        return txIds;
    }

    public static BtcBlockTxIds fromBytes(NetworkParameters params, byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        List<Sha256Hash> txIds = new ArrayList<>();
        while (buffer.hasRemaining()) {
            byte[] rawHashBytes = new byte[Sha256Hash.LENGTH];
            txIds.add(Sha256Hash.wrap(rawHashBytes));
        }
        return new BtcBlockTxIds(txIds);
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(txIds.size() * Sha256Hash.LENGTH);
        txIds.stream().forEach(txId -> {
            buffer.put(txId.getBytes());
        });
        return buffer.array();
    }
}
