package com.plooh.adssi.dial.data.domain;

import java.nio.ByteBuffer;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

public class BtcAddressTx {
    /// Can be used to map following version information
    /// 0 -> 00 Output fix length, no tx info
    /// 1 -> 01 Input fix length, no tx info
    /// 2 -> 10 Output variable length, with tx info
    /// 3 -> 11 Input variable length, with tx info
    private final int version;

    private final Sha256Hash txId;

    private final Sha256Hash blockId;

    private final Address address;

    private final Transaction tx;

    public BtcAddressTx(int version, Sha256Hash txId, Sha256Hash blockId, Address address, Transaction tx) {
        this.version = version;
        this.txId = txId;
        this.blockId = blockId != null ? blockId : Sha256Hash.ZERO_HASH;
        this.address = address;
        this.tx = tx;
    }

    static final int legacy_btc_addr_length = 1 + Sha256Hash.LENGTH + Sha256Hash.LENGTH + LegacyAddress.LENGTH;
    static final int legacy_btc_segwith_length = 1 + Sha256Hash.LENGTH + Sha256Hash.LENGTH
            + SegwitAddress.WITNESS_PROGRAM_LENGTH_PKH;

    public static BtcAddressTx fromBytes(NetworkParameters params, byte[] b) {
        if (b.length == legacy_btc_addr_length || b.length == legacy_btc_segwith_length) {
            return fromBytesV0(params, b);
        }

        ByteBuffer buffer = ByteBuffer.wrap(b);
        int version = buffer.getInt();
        int txIdLength = buffer.getInt();
        byte[] txId = null;
        if (txIdLength > 0) {
            txId = new byte[txIdLength];
            buffer.get(txId);
        }
        int blockIdLength = buffer.getInt();
        byte[] blockId = null;
        if (blockIdLength > 0) {
            blockId = new byte[blockIdLength];
            buffer.get(blockId);
        }
        int addrLength = buffer.getInt();
        Address address = null;
        if (addrLength > 0) {
            byte[] addressBytes = new byte[addrLength];
            buffer.get(addressBytes);
            if (addressBytes.length == LegacyAddress.LENGTH) {
                address = LegacyAddress.fromScriptHash(params, addressBytes);
            } else if (addressBytes.length == SegwitAddress.WITNESS_PROGRAM_LENGTH_PKH) {
                address = SegwitAddress.fromHash(params, addressBytes);
            }
        }
        int txLength = buffer.getInt();
        Transaction tx = null;
        if (txLength > 0) {
            byte[] txBytes = new byte[txLength];
            buffer.get(txBytes);
            tx = new Transaction(params, txBytes, 0);
        }
        return new BtcAddressTx(version, txId == null ? null : Sha256Hash.wrap(txId),
                blockId == null ? null : Sha256Hash.wrap(blockId), address,
                tx);
    }

    public static BtcAddressTx fromBytesV0(NetworkParameters params, byte[] b) {
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
        return new BtcAddressTx(input, Sha256Hash.wrap(txId), Sha256Hash.wrap(blockId), address, null);
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
