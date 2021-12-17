package com.plooh.adssi.dial.data.domain;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BtcBytesList {
    private final List<Bytes> txes = new ArrayList<>();

    public static BtcBytesList wrap(List<Bytes> txes) {
        BtcBytesList bl = new BtcBytesList();
        bl.txes.addAll(txes);
        return bl;
    }

    public List<Bytes> getTxes() {
        return txes;
    }

    public BtcBytesList merge(BtcBytesList b) {
        b.txes.forEach(e -> {
            if (!txes.contains(e))
                txes.add(e);
        });
        return this;
    }

    public BtcBytesList sort() {
        Collections.sort(txes);
        return this;
    }

    public static BtcBytesList fromBytes(byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        BtcBytesList btcBytesList = new BtcBytesList();
        while (buffer.hasRemaining()) {
            int itemLength = buffer.getInt();
            byte[] item = new byte[itemLength];
            buffer.get(item);
            btcBytesList.txes.add(Bytes.wrap(item));
        }
        return btcBytesList;
    }

    public byte[] toBytes() {
        int capacity = 0;
        for (int i = 0; i < txes.size(); i++) {
            capacity += 4;
            capacity += txes.get(i).length();
        }
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        for (int i = 0; i < txes.size(); i++) {
            Bytes b = txes.get(i);
            buffer.putInt(b.length());
            buffer.put(b.getBytes());
        }
        return buffer.array();
    }
}