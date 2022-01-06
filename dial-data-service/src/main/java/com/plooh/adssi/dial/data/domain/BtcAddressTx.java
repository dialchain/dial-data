package com.plooh.adssi.dial.data.domain;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

public class BtcAddressTx {
    public final static int VERSION_0_OUTPUT = 0;
    public final static int VERSION_0_INPUT = 1;
    public final static int VERSION_1_OUTPUT = 2;
    public final static int VERSION_1_INPUT = 3;
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
            + SegwitAddress.WITNESS_PROGRAM_MAX_LENGTH;

    public static BtcAddressTx fromBytes(NetworkParameters params, byte[] b) {
        if (b.length == legacy_btc_addr_length || b.length <= legacy_btc_segwith_length) {
            return fromBytesV0(params, b);
        }

        ByteBuffer buffer = ByteBuffer.wrap(b);
        // Read version byte
        int version = buffer.getInt();

        // Read the length of the txId byte
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
            var addressStr = new String(addressBytes, StandardCharsets.US_ASCII);
            address = Address.fromString(params, addressStr);
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
        } else {
            address = SegwitAddress.fromHash(params, addressBytes);
        }
        return new BtcAddressTx(input, Sha256Hash.wrap(txId), Sha256Hash.wrap(blockId), address, null);
    }

    public byte[] toBytes() {
        int intLength = 4;
        int versionLength = 4;
        int txIdLength = txId.getBytes().length;
        int blockIdLength = blockId.getBytes().length;
        byte[] addressBytes = address.toString().getBytes(StandardCharsets.US_ASCII);
        int addrLength = addressBytes.length;
        byte[] txBytes = tx == null ? new byte[0] : tx.unsafeBitcoinSerialize();
        int txLength = txBytes.length;

        int capacity = versionLength + intLength + txIdLength + intLength + blockIdLength + intLength + addrLength
                + intLength + txLength;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.putInt(version)
                .putInt(txIdLength).put(txId.getBytes())
                .putInt(blockIdLength).put(blockId.getBytes())
                .putInt(addrLength).put(addressBytes)
                .putInt(txLength).put(txBytes);
        return buffer.array();
    }

    public int getVersion() {
        return version;
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
}
