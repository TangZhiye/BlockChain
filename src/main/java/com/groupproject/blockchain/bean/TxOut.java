package com.groupproject.blockchain.bean;

import com.groupproject.blockchain.utils.RSAUtils;
import com.groupproject.blockchain.utils.Sha256Util;

import java.security.PublicKey;

public class TxOut {

    public String id;
    public PublicKey recipient; //also known as the new owner of these coins.
    public float value; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TxOut(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Sha256Util.applySha256(RSAUtils.getStringFromKey(recipient)+Float.toString(value)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

}
