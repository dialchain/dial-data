package com.plooh.adssi.dial.data.domain;

import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BTC_ADDRESS")
public class BtcAddress {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "TX_ID")
    private String txId;

    @Column(name = "BLOCK_ID")
    private String blockId;

    private boolean input;

}
