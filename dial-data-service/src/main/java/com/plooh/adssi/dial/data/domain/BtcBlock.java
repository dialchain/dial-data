package com.plooh.adssi.dial.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BTC_BLOCKS")
public class BtcBlock {

    @Id
    @Column(name = "BLOCK_ID")
    private String blockId;

    @Column(name = "BLOCK_BYTES")
    private byte[] blockBytes;

}
