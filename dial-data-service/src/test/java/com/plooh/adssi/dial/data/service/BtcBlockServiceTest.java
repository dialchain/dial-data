package com.plooh.adssi.dial.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.plooh.adssi.dial.data.domain.BtcBlockHeader;
import com.plooh.adssi.dial.data.domain.BtcTransaction;
import com.plooh.adssi.dial.data.store.BtcBlockStore;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BtcBlockServiceTest {

    @Mock
    private BtcBlockStore btcBlockStore;

    @InjectMocks
    private BtcBlockService uut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveTransaction() {
        uut.saveTransaction(new BtcTransaction());
        verify(btcBlockStore).save(any());
    }

    @Test
    public void shouldCheckTransaction() {
        when(btcBlockStore.find("txId"))
            .thenReturn(BtcTransaction.builder().blockIds(Set.of("blockId")).build());

        var response = uut.checkTransaction("txId");
        assertThat(response.getReportingBlocks()).isEqualTo(1);
    }

    @Test
    public void shouldGetBlocksByHeight() {
        when(btcBlockStore.getBlocksByHeight(0, 10))
            .thenReturn(List.of(BtcBlockHeader.builder().blockId("blockId1").build(),
                BtcBlockHeader.builder().blockId("blockId2").build()));

        var response = uut.getBlocksByHeight(0, 10);
        assertThat(response.getBlocks().size()).isEqualTo(2);
    }

    @Test
    public void shouldGetBlocksByTime() {
        when(btcBlockStore.getBlocksByTime(0, 10))
            .thenReturn(List.of(BtcBlockHeader.builder().blockId("blockId1").build(),
                BtcBlockHeader.builder().blockId("blockId2").build()));

        var response = uut.getBlocksByTime(0, 10);
        assertThat(response.getBlocks().size()).isEqualTo(2);
    }

}
