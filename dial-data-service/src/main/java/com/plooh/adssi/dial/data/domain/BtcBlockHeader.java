package com.plooh.adssi.dial.data.domain;

import java.util.Set;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BTC_BLOCK_HEADERS")
public class BtcBlockHeader {

    @Id
    @Column(name = "BLOCK_ID")
    private String blockId;

    @Column(name = "PREV_BLOCK_ID")
    private String prevBlockHash;

    @Column(name = "BLOCK_HEIGHT")
    private Integer height;

    @Column(name = "TIME")
    private Integer time;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "BTC_BLOCKS_TRANSACTIONS", joinColumns = @JoinColumn(name = "BLOCK_ID"))
    @Column(name = "TX_ID")
    private Set<String> txIds;

}
