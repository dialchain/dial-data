package com.plooh.adssi.dial.data.domain;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

@RequiredArgsConstructor
@Getter
public enum BitcoinNetworkEnum {
    MAIN("main"),
    PROD("main"), // alias for MAIN
    TEST("testnet"),
    REGTEST("regtest");

    private final String net;

    public static Optional<BitcoinNetworkEnum> fromValue(String value) {
        return Stream.of(values())
            .filter(it -> it.getNet().equals(value))
            .findAny();
    }

    public NetworkParameters get() {
        switch(this) {
            case MAIN:
            case PROD:
                return MainNetParams.get();
            case TEST:
                return TestNet3Params.get();
            case REGTEST:
            default:
                return RegTestParams.get();
        }
    }

}
