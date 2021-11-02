package com.plooh.adssi.dial.data.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class BtcTransaction {

    private String id;

    private byte[] transactionBytes;

    private Set<String> peerIds;

    private Set<String> blockIds;

    public void addPeer(String peerId){
        if (this.getPeerIds() == null){
            this.setPeerIds(new HashSet<>());
        }
        this.getPeerIds().add(peerId);
    }

    public void addBlockId(String blockId){
        if (this.getBlockIds() == null){
            this.setBlockIds(new HashSet<>());
        }
        this.getBlockIds().add(blockId);
    }

    public void addBestBlockId(String blockId){
        this.setPeerIds(new HashSet<>());
        this.setBlockIds(new HashSet<>());
        this.getBlockIds().add(blockId);
    }

}
