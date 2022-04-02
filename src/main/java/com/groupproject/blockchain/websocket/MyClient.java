package com.groupproject.blockchain.websocket;


import com.groupproject.blockchain.Tools.MessageBean;
import com.groupproject.blockchain.Tools.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MyClient extends WebSocketClient {
    private String name;

    public MyClient(URI serverUri, String name) {
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
