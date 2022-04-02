package com.groupproject.blockchain.websocket;


import com.groupproject.blockchain.Tools.MessageBean;
import com.groupproject.blockchain.Tools.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupproject.blockchain.bean.Block;
import com.groupproject.blockchain.bean.Wallet;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Security;

public class RandomTransactionGenerator extends WebSocketClient {
    private String name;

    public RandomTransactionGenerator(URI serverUri, String name) {
        super(serverUri);
        this.name = name;
    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("open the connection");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("client:" + name + "receive message:" + message);


    }
    public void broadTransaction(Transaction transaction){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // transfer the transaction data to String
            String transactionData = objectMapper.writeValueAsString(transaction);
            //put string into the message bean
            MessageBean messageBean = new MessageBean(1, transactionData);
            String msg = objectMapper.writeValueAsString(messageBean);
            send(msg);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public void broadBlock(Block block){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // transfer the transaction data to String
            String blockData = objectMapper.writeValueAsString(block);
            //put string into the message bean
            MessageBean messageBean = new MessageBean(2, blockData);

            String msg = objectMapper.writeValueAsString(messageBean);
            send(msg);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("client" + name + "close the connect");
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("client" + name + "error!");
        ex.printStackTrace();
    }


    public static void main(String[] args) {
        URI uri = null;
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            //1. Create new Wallets
            Wallet walletA = new Wallet();
            Wallet walletB = new Wallet();
            //2. Generate Genesis Block
            Block genesisBlock = new Block("0", 0, 1);
            genesisBlock.addCoinbaseTx(walletA);
            genesisBlock.mineBlock();

            // Generate Connection
            uri = new URI("ws://localhost:8082");
            RandomTransactionGenerator client1 = new RandomTransactionGenerator(uri, "client1");
            client1.connect();
            Thread.sleep(1000);

            //Transfer Block
           client1.broadBlock(genesisBlock);


            //The transaction is just a sample test, you can replace with our new Transaction class
/*
            Transaction transaction = new Transaction(1,"sasa");
            client1.broadTransaction(transaction);
*/
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
