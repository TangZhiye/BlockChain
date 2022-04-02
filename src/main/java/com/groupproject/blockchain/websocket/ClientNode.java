package com.groupproject.blockchain.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupproject.blockchain.Tools.MessageBean;
import com.groupproject.blockchain.Tools.Transaction;
import com.groupproject.blockchain.bean.Block;
import com.groupproject.blockchain.bean.TxOut;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.groupproject.blockchain.bean.PoW.getDifficulty;

public class ClientNode extends WebSocketClient {
    private String name;
    public ArrayList<Block> blockChain = new ArrayList<Block>();
    public HashMap<String, TxOut> UTXOs = new HashMap<String, TxOut>();

    public float minimumTransaction = 0.1f;

    //Config for the Blockchain
    public final int blockGenerationInterval = 10; // we expect that evey 10 seconds we find a block
    public final int  diffAdjustInterval= 1; // defines how often the difficulty should be adjusted with the increasing or decreasing network hashrate.

    public ArrayList<Transaction> transactionPool = new ArrayList<Transaction>();
    public ClientNode(URI serverUri, String name) {
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




    public static void main(String[] args) {
        URI uri = null;
        try {
            uri = new URI("ws://localhost:8082");
            MyClient client1 = new MyClient(uri, "client1");
            client1.connect();
            Thread.sleep(1000);
            //The transaction is just a sample test, you can replace with our new Transaction class
            Transaction transaction = new Transaction(1,"sasa");
            client1.broadTransaction(transaction);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}