package com.groupproject.blockchain.bean;

import java.io.Serializable;

public class TxIn implements Serializable {
    public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    public TxOut UTXO; //Contains the Unspent transaction output

    public TxIn(){

    }
    public TxIn(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    @Override
    public String toString() {
        return "TxIn{" +
                "transactionOutputId='" + transactionOutputId + '\'' +
                ", UTXO=" +
                '}';
    }
}
