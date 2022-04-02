package com.groupproject.blockchain.bean;

public class TxIn {
    public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    public TxOut UTXO; //Contains the Unspent transaction output

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
