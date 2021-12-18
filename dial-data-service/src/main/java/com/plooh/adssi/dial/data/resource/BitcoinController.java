package com.plooh.adssi.dial.data.resource;

import java.io.IOException;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.exception.AddressNotFound;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.NotChainHead;
import com.plooh.adssi.dial.data.exception.TransactionNotFound;
import com.plooh.adssi.dial.data.repository.BtcBlockStore;
import com.plooh.adssi.dial.data.service.PeerGroupService;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Utils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final BtcBlockStore btcBlockService;
    private final PeerGroupService peerGroupService;

    @Override
    public ResponseEntity<Void> broadcastTransaction(MultipartFile txBytes) {
        try {
            peerGroupService.broadcastTransaction(txBytes.getBytes());
            return ResponseEntity.accepted().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> getChainhead() {
        return res(btcBlockService.getChainhead().orElseThrow(() -> new NotChainHead()), "chainhead.dat");
    }

    private ResponseEntity<Resource> res(byte[] bytes, String fileName) {
        Resource resource = new ByteArrayResource(bytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", fileName));
        try {
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> getBlockHeadersForBlockHash(String blockHash) {
        var response = btcBlockService.getBlockHeadersForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".header.dat");
    }

    @Override
    public ResponseEntity<Resource> getBlockHashForTxId(String txId) {
        var response = btcBlockService.getBlockHashForTxId(Utils.HEX.decode(txId))
                .orElseThrow(() -> new TransactionNotFound(txId));
        return res(response, txId + ".blockHash.dat");
    }

    @Override
    public ResponseEntity<Resource> getTxsForAddress(String address) {

        var response = btcBlockService
                .getTxsForAddress(Address.fromString(btcBlockService.getParams(), address).getHash())
                .orElseThrow(() -> new AddressNotFound(address));
        return res(response, address + ".txIds.dat");
    }

    @Override
    public ResponseEntity<Resource> getBlockForBlockHash(String blockHash) {
        var response = btcBlockService.getBlockForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".block.dat");
    }

    @Override
    public ResponseEntity<Resource> getTxIdsForBlockHash(String blockHash) {
        var response = btcBlockService.getTxIdsForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".txIds.dat");
    }

}
