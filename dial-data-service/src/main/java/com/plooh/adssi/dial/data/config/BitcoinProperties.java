package com.plooh.adssi.dial.data.config;

import com.plooh.adssi.dial.data.domain.enums.BitcoinNetworkEnum;
import lombok.Data;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@Configuration
@ConfigurationProperties("dial.data.bitcoin")
public class BitcoinProperties {

    @NotNull
    private String network;

    @NotNull
    private Boolean localhost;

    @NotNull
    private String fastCatchupTime;

    @NotNull
    private String blockstoreDir;

    @NotNull
    private String dialblockstoreDir;

    public NetworkParameters getParams(){
        // Figure out which network we should connect to.
        BitcoinNetworkEnum bitcoinNetworkEnum = BitcoinNetworkEnum.fromValue(network)
            .orElseGet(() -> BitcoinNetworkEnum.MAIN);
        return bitcoinNetworkEnum.get();
    }

}
