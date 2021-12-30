package com.plooh.adssi.dial.data.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.validation.Valid;

import com.plooh.adssi.dial.data.bitcoin.BitcoinApi;
import com.plooh.adssi.dial.data.exception.AddressNotFound;
import com.plooh.adssi.dial.data.exception.BlockNotFound;
import com.plooh.adssi.dial.data.exception.NotChainHead;
import com.plooh.adssi.dial.data.exception.PayloadLarge;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@RestController
public class BitcoinController implements BitcoinApi {

    private final BtcBlockStore btcBlockStore;
    private final PeerGroupService peerGroupService;
    // private final HttpServletRequest request;

    /**
     * POST /btc/transactions : Broadcast a transaction to the bitcoin network
     * Relay transactions produced by the SPV client to the bitcoin network.
     *
     * @param body Transaction to be forwarded to the bitcoin network. (required)
     * @return Transaction successfully submitted. (status code 202)
     */
    @ApiOperation(value = "Broadcast a transaction to the bitcoin network", nickname = "broadcastTransaction", notes = "Relay transactions produced by the SPV client to the bitcoin network. ", tags = {
            "bitcoin", })
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Transaction successfully submitted.") })
    @RequestMapping(value = "/btc/transactions", consumes = { "application/octet-stream" }, method = RequestMethod.POST)
    public ResponseEntity<Void> _broadcastTransaction(
            @ApiParam(value = "Transaction to be forwarded to the bitcoin network.", required = true) @Valid @RequestBody Resource body) {
        return broadcastTransaction(body);
    }

    // Override this method
    // default ResponseEntity<Void> broadcastTransaction(MultipartFile body) {
    // return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    // }

    public ResponseEntity<Void> broadcastTransaction(Resource body) {
        try {
            int contentLength = (int) body.contentLength();
            if (contentLength > (5 * 1024)) {
                throw new PayloadLarge();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(contentLength);
            body.getInputStream().transferTo(bos);
            peerGroupService.broadcastTransaction(bos.toByteArray());
            return ResponseEntity.accepted().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> getChainhead() {
        return res(btcBlockStore.getChainhead().orElseThrow(() -> new NotChainHead()), "chainhead.dat");
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
        var response = btcBlockStore.getBlockHeadersForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".header.dat");
    }

    @Override
    public ResponseEntity<Resource> getBlockHashForTxId(String txId) {
        var response = btcBlockStore.getBlockHashForTxId(Utils.HEX.decode(txId))
                .orElseThrow(() -> new TransactionNotFound(txId));
        return res(response, txId + ".blockHash.dat");
    }

    @Override
    public ResponseEntity<Resource> getTxsForAddress(String address) {

        var response = btcBlockStore
                .getTxsForAddress(Address.fromString(btcBlockStore.getParams(), address))
                .orElseThrow(() -> new AddressNotFound(address));
        return res(response, address + ".txIds.dat");
    }

    @Override
    public ResponseEntity<Resource> getBlockForBlockHash(String blockHash) {
        var response = btcBlockStore.getBlockForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".block.dat");
    }

    @Override
    public ResponseEntity<Resource> getTxIdsForBlockHash(String blockHash) {
        var response = btcBlockStore.getTxIdsForBlockHash(Utils.HEX.decode(blockHash))
                .orElseThrow(() -> new BlockNotFound(blockHash));
        return res(response, blockHash + ".txIds.dat");
    }

}
