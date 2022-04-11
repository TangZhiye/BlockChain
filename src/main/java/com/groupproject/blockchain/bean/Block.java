package com.groupproject.blockchain.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.groupproject.blockchain.utils.MerkleTreeUtil;
import com.groupproject.blockchain.utils.RSAUtils;
import com.groupproject.blockchain.utils.Sha256Util;
import com.groupproject.blockchain.utils.StringUtil;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

//Block Structure
public class Block implements Serializable {
    //The height of block, start from 0
    public int index;
    //UnixTime
    public long timeStamp;
    public String hash;
    //Encrypted via Algo SHA-256
    public String previousHash;
    //Any data, including Txs stored here (merkleRoot)
    public String data;
    public int nonce;
    //Store transactions
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    //Difficulty
    public int difficulty;

    //Bean Constructor
    public  Block(){

    }
    //Block constructor
    public Block(String previousHash, int index, int difficulty){
        this.index = index;
        this.timeStamp = new Date().getTime()/1000;
        this.hash = getHash();
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        //this.data = data;
    }

    //Block Hash
    public String getHash(){
        String value = Sha256Util.applySha256(
                Integer.toString(index)+ Long.toString(timeStamp)
                + previousHash + Integer.toString(nonce) + Integer.toString(difficulty) +
                        data
        );
        return value;
    }

    //For validation test
    public String hashTest(String index, String timeStamp, String previousHash, String data){
        return hash = Sha256Util.applySha256(index+ timeStamp+ previousHash+ data);
    }

    //Increases nonce value until hash target is reached.
    public void mineBlock() {

        int difficulty = this.difficulty;
        data = MerkleTreeUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty); //Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = getHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    //Add regular transactions to this block
    public boolean addTransaction(Transaction transaction) {
        // regular Tx only can be added after coinbase Tx
        if (transaction == null || transactions.isEmpty()) {
            System.out.println("Regular Transaction failed to add.");
            return false;}
//        if ((!previousHash.equals("0"))) {
//        }
        if ((!transaction.processTransaction())) {
            System.out.println("Transaction failed to process. Discarded.");
            return false;
        }
        transactions.add(transaction);
        System.out.println("Regular Transaction Successfully added to Block");
        return true;
    }

    // Add coinbase transactions to this block
    public boolean addCoinbaseTx(Wallet wallet){
        if(!transactions.isEmpty()) {
            System.out.println("Coinbase Transaction failed to add.");
            return false;}
        Wallet coinbase = new Wallet();
        Transaction coinbaseTx = new Transaction(RSAUtils.getStringFromKey(coinbase.publicKey), coinbase.publicKey, RSAUtils.getStringFromKey(wallet.publicKey), 50f );
        coinbaseTx.isCoinbaseTx = true;
        coinbaseTx.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
//        coinbaseTx.transactionId = "0"; //manually set the transaction id 要改成交易hash
        coinbaseTx.transactionId = Sha256Util.applySha256(RSAUtils.getStringFromKey(coinbase.publicKey) +
                RSAUtils.getStringFromKey(wallet.publicKey) + Float.toString(50) + index);
        coinbaseTx.outputs.add(new TxOut(coinbaseTx.recipient, coinbaseTx.value, coinbaseTx.transactionId)); //manually add the Transactions Output
        transactions.add(coinbaseTx);
        //Store coinbase transaction in the BlockChain UTXOs list.
        BlockChain.UTXOs.put(coinbaseTx.outputs.get(0).id, coinbaseTx.outputs.get(0));
        System.out.println("Coinbase Transaction Successfully added to Block");
        return true;
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index;
    }
}
