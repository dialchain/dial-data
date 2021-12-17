package com.plooh.adssi.dial.data.domain;

import java.util.Arrays;

import org.bitcoinj.core.Utils;

public class Bytes implements Comparable<Bytes> {
    private final byte[] bytes;

    public Bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public static Bytes wrap(byte[] bytes) {
        return new Bytes(bytes);
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Arrays.equals(bytes, ((Bytes) o).bytes);
    }

    /**
     * Returns the last four bytes of the wrapped hash. This should be unique enough
     * to be a suitable hash code even for
     * blocks, where the goal is to try and get the first bytes to be zeros (i.e.
     * the value as a big integer lower
     * than the target value).
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return Utils.HEX.encode(bytes);
    }

    /**
     * Returns the internal byte array, without defensively copying. Therefore do
     * NOT modify the returned array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int compareTo(final Bytes other) {
        return Arrays.compare(bytes, other.bytes);
    }
}