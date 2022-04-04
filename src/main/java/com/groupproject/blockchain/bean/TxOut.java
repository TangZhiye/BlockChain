package com.groupproject.blockchain.bean;

import com.groupproject.blockchain.utils.RSAUtils;
import com.groupproject.blockchain.utils.Sha256Util;

import java.io.Serializable;
import java.security.PublicKey;

public class TxOut implements Serializable {

    public String id;
    public String recipient; //also known as the new owner of these coins.
    public float value; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    public TxOut(){

    }
    //Constructor
    public TxOut(String recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Sha256Util.applySha256((recipient)+Float.toString(value)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(String publicKey) {
         System.out.println(publicKey.equals(recipient));
        return (publicKey.equals(recipient));
    }

}
