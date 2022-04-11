package com.groupproject.blockchain.websocket;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupproject.blockchain.Tools.MessageBean;
import com.groupproject.blockchain.bean.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import static com.groupproject.blockchain.bean.BlockChain.blockChain;
import static com.groupproject.blockchain.bean.PoW.getDifficulty;

public class ClientNode extends WebSocketClient {

    private String name;
/*
    public ArrayList<Block> blockChain = new ArrayList<Block>();
    public HashMap<String, TxOut> UTXOs = new HashMap<String, TxOut>();
*/


    //Wallet des Client Nodes
    Wallet wallet;
    //Config for the Blockchain
    public final int blockGenerationInterval = 10; // we expect that evey 10 seconds we find a block
    public final int  diffAdjustInterval= 1; // defines how often the difficulty should be adjusted with the increasing or decreasing network hashrate.
    public final int transactionsPerBlock = 1;
    public float minimumTransaction = 0.1f;


    public ArrayList<Transaction> transactionPool = new ArrayList<Transaction>();



    public ClientNode(URI serverUri, String name, Wallet wallet) {
        super(serverUri);
        this.name = name;
        this.wallet = wallet;


    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("open the connection");

    }

    @Override
    public void onMessage(String message) {
        System.out.println("client: " + name + " receive message:" + message);
        try {
            //1. transfer message to bean
            ObjectMapper objectMapper = new ObjectMapper();
            MessageBean messageBean = objectMapper.readValue(message, MessageBean.class);
            //2. According to the bean.type do different transfer job
            //  1 -- > Transaction
            //  2 -- > 区块
            if(messageBean.type == 1){
                //Return current blockchain
                System.out.println(messageBean.msg);
                Transaction transaction = objectMapper.readValue(messageBean.msg, Transaction.class);
                transactionPool.add(transaction);
                System.out.println(transaction.toString());
                if (transactionPool.size() >= transactionsPerBlock){
                    Block block = generateNextBlock();
                    block.addCoinbaseTx(this.wallet);
                    ArrayList<Transaction> toDelete = new ArrayList<Transaction>();
                    for (Transaction currentTransaction: transactionPool){
                       boolean retValue = block.addTransaction(currentTransaction);
                       if (retValue){
                           toDelete.add(currentTransaction);
                       }
                    }
                    transactionPool.removeAll(toDelete);
                    block.mineBlock();
                    blockChain.add(block);
                    broadBlock(block);


                }
            }
            else if(messageBean.type == 2){
                System.out.println(messageBean.msg);
                Block block = objectMapper.readValue(messageBean.msg, Block.class);
                blockChain.add(block);
                isValidNewBlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void broadBlock(Block block) {
        ObjectMapper objectMapper = new ObjectMapper();
        // transfer the transaction data to String
        String blockData = null;
        try {
            blockData = objectMapper.writeValueAsString(block);
            //put string into the message bean
            MessageBean messageBean = new MessageBean(2, blockData);
            String msg = objectMapper.writeValueAsString(messageBean);
            send(msg);
            System.out.println("Broadcasted new Block to the network");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
        public void broadTransaction(Transaction transaction) {
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

    public  Block generateNextBlock(){
        Block previousBlock = blockChain.get(blockChain.size()-1);
        int nextIndex = previousBlock.index+1;
        String previousHash = blockChain.get(blockChain.size()-1).hash;

        //generate the new difficulty for the block -> store the difficulty in the block

        int difficulty;
        if (nextIndex > 2){
            Block prePreviousBlock = blockChain.get(blockChain.size()-2);
            difficulty = getDifficulty(prePreviousBlock.timeStamp,previousBlock.timeStamp,previousBlock.difficulty,previousBlock.index,blockGenerationInterval,diffAdjustInterval);
        } else{
            difficulty = 1;

        };

        //Here: you have to set the difficulty based on the blocks
        Block newBlock = new Block(previousHash, nextIndex, difficulty);
        return newBlock;
    }

    public  boolean isValidNewBlock(){
        Block previousBlock;
        Block newBlock;
        //a temporary working list of unspent transactions at a given block state.
//        HashMap<String, TxOut> tempUTXOs = new HashMap<String, TxOut>();
//        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for(int i=1; i< blockChain.size(); i++){
            previousBlock = blockChain.get(i-1);
            newBlock = blockChain.get(i);
            String testIndex = Integer.toString(newBlock.index);
            String testTimestamp = Long.toString(newBlock.timeStamp);
            String testPre = newBlock.previousHash;
            String testData = newBlock.data;
            //index verification
            if(previousBlock.index!=newBlock.index-1){
                System.out.println("Index error!");
                return false;
            }
            //Hash linkage verification
            if(!(previousBlock.hash).equals(newBlock.previousHash)){
                System.out.println("Hash linkage verification fails!");
                System.out.println(newBlock.index);
                return false;
            }

        }
        return true;
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


    //What does the client node do after startup?
    // 1. Get current blockchain from the other running nodes
    // 2. If there is no blockchain yet -> create genesis block
    // 3. Listen to incoming transactions and store them in an array
    // 4. When there are enough transactions -> create a new block
    // 5. Store the new block in the blockchain
    // 6. Broadcast the new block


    //Helper Methods
    //Get longest Chain -> iterate through the array and get the last block of the longest path


    //Steps:
    //1. Create communication in the client node
        //a) Client node must be able to receive the whole blockchain during startup
        //b) Client nodes listens to incoming transactions -> if there are two -> creates new block
        //c) After the mining is successful -> client node broadcasts its block to the blockchain


    public static void main(String[] args) {
        URI uri = null;
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            uri = new URI("ws://localhost:8082");
            Wallet walletA = new Wallet();
            ClientNode client1 = new ClientNode(uri, "client1", walletA);

            client1.connect();
            Thread.sleep(1000);
            //The transaction is just a sample test, you can replace with our new Transaction class
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
