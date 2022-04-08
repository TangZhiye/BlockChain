package com.groupproject.blockchain.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.groupproject.blockchain.utils.RSAUtils;
import com.groupproject.blockchain.utils.Sha256Util;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

@JsonIgnoreProperties({"senderObject"})
public class Transaction implements Serializable {

    public String transactionId; //Contains a hash of transaction*
    public String sender; //Senders address/public key.
    public PublicKey senderObject; //Senders address/public key.
    public String recipient; //Recipients address/public key.
    public float value; //Contains the amount we wish to send to the recipient.
    public byte[] signature; //This is to prevent anybody else from spending funds in our wallet.

    public ArrayList<TxIn> inputs = new ArrayList<TxIn>();
    public ArrayList<TxOut> outputs = new ArrayList<TxOut>();

    private static int sequence = 0; //A rough count of how many transactions have been generated
    public boolean isCoinbaseTx = false;

    //Bean Constructor
    public Transaction(){

    }

    //For Coinbase Transactions
    public Transaction(String from, PublicKey senderObject, String to, float value) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.senderObject = senderObject;
    }
    // Constructor:
    public Transaction(String from, PublicKey senderObject, String to, float value, ArrayList<TxIn> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
        this.senderObject = senderObject;
    }

    // Called when adding transaction to block
    public boolean processTransaction() {
        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
        //Gather transaction inputs (Making sure they are unspent):
        for(TxIn i : inputs) {
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }
        //Check if transaction is valid:
        if(returnInputsValue() < BlockChain.minimumTransaction) {
            System.out.println("Transaction Inputs too small: " + returnInputsValue());
            return false;
        }
        //Generate transaction outputs:
        float leftOver = returnInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TxOut( this.recipient, value,transactionId)); //send value to recipient
        outputs.add(new TxOut( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender
        //Add outputs to Unspent list
        for(TxOut o : outputs) {
            BlockChain.UTXOs.put(o.id , o);
        }
        //Remove transaction inputs from UTXO lists as spent:
        for(TxIn i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            BlockChain.UTXOs.remove(i.UTXO.id);
        }
        return true;
    }

    public float returnInputsValue() {
        float total = 0;
        for(TxIn i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it, This behavior may not be optimal.
            total += i.UTXO.value;
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = sender + recipient + Float.toString(value);
        signature = RSAUtils.getSignature("ECDSA", privateKey, data);
    }

    public boolean verifySignature() {
        String data = sender + recipient + Float.toString(value);
        return RSAUtils.verifySignature("ECDSA", this.sender, data, signature);
    }

    public float returnOutputsValue() {
        float total = 0;
        for(TxOut o : outputs) {
            total += o.value;
        }
        return total;
    }

    private String calculateHash() {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return Sha256Util.applySha256(sender+
                recipient + Float.toString(value) + sequence);
    }
}
