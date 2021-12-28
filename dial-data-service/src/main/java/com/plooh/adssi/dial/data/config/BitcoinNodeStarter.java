package com.plooh.adssi.dial.data.config;

import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.utils.BriefLogFormatter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BitcoinNodeStarter {

    private final PeerGroup peerGroup;

    @PostConstruct
    private void startNode() {
        BriefLogFormatter.init();
        // Starts the PeerGroup and begins network activity.
        peerGroup.start();

        CompletableFuture.runAsync(() -> {
            peerGroup.downloadBlockChain();
            log.info("=== Download BlockChain completed... ===");
        });
    }

}
