package com.plooh.adssi.dial.data.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BTC_TRANSACTIONS")
public class BtcTransaction {

    @Id
    @Column(name = "TX_ID")
    private String txId;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "BTC_TRANSACTIONS_BLOCKS", joinColumns = @JoinColumn(name = "TX_ID"))
    @Column(name = "BLOCK_ID")
    private Set<String> blockIds;

    public void addBlockId(String blockId){
        if (this.getBlockIds() == null){
            this.setBlockIds(new HashSet<>());
        }
        this.getBlockIds().add(blockId);
    }

}
