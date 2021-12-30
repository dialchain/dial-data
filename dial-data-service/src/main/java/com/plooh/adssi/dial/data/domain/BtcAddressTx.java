package com.plooh.adssi.dial.data.domain;

import java.nio.ByteBuffer;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Sha256Hash;

public class BtcAddressTx {

    private final boolean input;

    private final Sha256Hash txId;

    private final Sha256Hash blockId;

    private final Address address;

    public BtcAddressTx(boolean input, Sha256Hash txId, Sha256Hash blockId, Address address) {
        this.input = input;
        this.txId = txId;
        this.blockId = blockId != null ? blockId : Sha256Hash.ZERO_HASH;
        this.address = address;
    }

    public static BtcAddressTx fromBytes(NetworkParameters params, byte[] b) {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        byte input = buffer.get(0);
        int offset = 1;
        byte[] txId = new byte[Sha256Hash.LENGTH];
        buffer.get(txId);
        offset += Sha256Hash.LENGTH;
        byte[] blockId = new byte[Sha256Hash.LENGTH];
        buffer.get(blockId);
        offset += Sha256Hash.LENGTH;
        byte[] addressBytes = new byte[b.length - offset];
        buffer.get(addressBytes);
        Address address = null;
        if (addressBytes.length == LegacyAddress.LENGTH) {
            address = LegacyAddress.fromScriptHash(params, addressBytes);
        } else if (addressBytes.length == SegwitAddress.WITNESS_PROGRAM_LENGTH_PKH) {
            address = SegwitAddress.fromHash(params, addressBytes);
        }
        return new BtcAddressTx(input != 0, Sha256Hash.wrap(txId), Sha256Hash.wrap(blockId), address);
    }

    public byte[] toBytes() {
        byte[] addressBytes = address.getHash();
        int capacity = 1 + Sha256Hash.LENGTH + Sha256Hash.LENGTH + addressBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put((byte) (input ? 1 : 0));
        buffer.put(txId.getBytes());
        buffer.put(blockId.getBytes());
        buffer.put(addressBytes);
        return buffer.array();
    }

    public boolean isInput() {
        return input;
    }

    public Sha256Hash getTxId() {
        return txId;
    }

    public Sha256Hash getBlockId() {
        return blockId;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
        result = prime * result + (input ? 1231 : 1237);
        result = prime * result + ((txId == null) ? 0 : txId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BtcAddressTx other = (BtcAddressTx) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (blockId == null) {
            if (other.blockId != null)
                return false;
        } else if (!blockId.equals(other.blockId))
            return false;
        if (input != other.input)
            return false;
        if (txId == null) {
            if (other.txId != null)
                return false;
        } else if (!txId.equals(other.txId))
            return false;
        return true;
    }

}
