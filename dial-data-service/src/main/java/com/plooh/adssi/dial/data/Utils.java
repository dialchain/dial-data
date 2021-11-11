package com.plooh.adssi.dial.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

@Slf4j
public class Utils {

    public static int computeBlockHeightFromDate(Date date, NetworkParameters parameters){
        Long blockHeight = ( ( date.getTime() / 1000 ) - parameters.getGenesisBlock().getTimeSeconds() ) / 10*60*60 ;
        return blockHeight.intValue();
    }


    public static List<Transaction> getTransactionsFromBytes(byte[] transactionBytes, NetworkParameters parameters){
        List<Transaction> transactionList = new LinkedList<>();
        if (transactionBytes == null){
            return transactionList;
        }

        int numTxn = (int) org.bitcoinj.core.Utils.readUint32(transactionBytes, 0);
        int offset = 4;
        for (int i = 0; i < numTxn; i++) {
            Transaction tx = parameters.getDefaultSerializer().makeTransaction(transactionBytes, offset);
            transactionList.add(tx);
            offset += tx.getMessageSize();
        }
        return transactionList;
    }

    public static byte[] getTransactionAsBytes(Transaction tx) {
        byte[] transactions = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            tx.bitcoinSerialize(bos);
            transactions = bos.toByteArray();
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
        return transactions;
    }


}
