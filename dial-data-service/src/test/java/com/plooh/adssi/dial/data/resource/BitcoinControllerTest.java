package com.plooh.adssi.dial.data.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plooh.adssi.dial.data.bitcoin.model.*;
import com.plooh.adssi.dial.data.service.BtcBlockService;
import com.plooh.adssi.dial.data.service.PeerGroupService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

public class BitcoinControllerTest {

    @Mock
    private BtcBlockService btcBlockService;

    @Mock
    private PeerGroupService peerGroupService;

    @InjectMocks
    private BitcoinController uut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSubmitTransaction() {
        var actual = uut.submitTransaction(new BtcTransactionDto().transactionBytes("test".getBytes(StandardCharsets.UTF_8)));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(peerGroupService).submitTransaction(any());
    }

    @Test
    public void shouldCheckTransaction() {
        var response = new BtcCheckTransactionResponse();
        when(btcBlockService.checkTransaction("txId")).thenReturn(response);
        var actual = uut.checkTransaction("txId");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isSameAs(response);
        verify(btcBlockService).checkTransaction("txId");
    }

    @Test
    public void shouldGetBlockByTransactionId() {
        var response = new BtcFindBlockResponse();
        when(btcBlockService.findBlockByTransactionId("txId")).thenReturn(response);
        var actual = uut.getBlockByTransactionId("txId");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isSameAs(response);
        verify(btcBlockService).findBlockByTransactionId("txId");
    }

    @Test
    public void shouldGetBlocksByHeight() {
        var response = new BtcBlockHeadersResponse();
        when(btcBlockService.getBlocksByHeight(1, 10)).thenReturn(response);
        var actual = uut.getBlocksByHeight(1, 10);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isSameAs(response);
        verify(btcBlockService).getBlocksByHeight(1, 10);
    }

    @Test
    public void shouldGetBlock() {
        var response = new BtcBlockDto();
        when(btcBlockService.getBlock("blockId")).thenReturn(response);
        var actual = uut.getBlock("blockId");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isSameAs(response);
        verify(btcBlockService).getBlock("blockId");
    }

    @Test
    public void shouldGetBlocksByTime() {
        var response = new BtcBlockHeadersResponse();
        when(btcBlockService.getBlocksByTime(1, 10)).thenReturn(response);
        var actual = uut.getBlocksByTime(1, 10);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isSameAs(response);
        verify(btcBlockService).getBlocksByTime(1, 10);
    }

}
