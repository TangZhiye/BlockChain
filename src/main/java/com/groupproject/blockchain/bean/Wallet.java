package com.groupproject.blockchain.bean;

import com.groupproject.blockchain.utils.RSAUtils;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 钱包
public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;
    private String name;
    // UTXOs of this wallet
    public HashMap<String, TxOut> UTXOs = new HashMap<String, TxOut>();

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Transaction sendFunds(PublicKey recipient,float value ) {
        float balance = getBalance();
        if(balance < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TxIn> inputs = new ArrayList<TxIn>();
        // Gather enough UTXOs from this wallet
        float total = 0;
        for (Map.Entry<String, TxOut> item: UTXOs.entrySet()){
            TxOut UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TxIn(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(RSAUtils.getStringFromKey(publicKey), publicKey, RSAUtils.getStringFromKey(recipient), value, inputs);
        // This signature ensured the transaction is only valid if a wallet only gather its own UTXOs in transaction.
        // If a evil wallet try to gather UTXOs with others public key, he can put it in transaction,
        // but only can sign with his own private key. When adding this transaction into block and
        // verify signature, the sender is others public key, but the signature is his own, So verify fail.
        newTransaction.generateSignature(privateKey);

        for(TxIn input: inputs){
            UTXOs.remove(input.transactionOutputId); // remove those UTXOs from wallet which already become input
        }

        return newTransaction;
    }

    // Get all UTXOs belong to this public key from Chain and put in this wallet ()
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TxOut> item: BlockChain.UTXOs.entrySet()){
            TxOut UTXO = item.getValue();
            if(UTXO.isMine(RSAUtils.getStringFromKey(publicKey))) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.value ;
            }
        }
        return total;
    }
}
