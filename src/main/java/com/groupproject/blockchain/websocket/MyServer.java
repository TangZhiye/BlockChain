package com.groupproject.blockchain.websocket;


import com.groupproject.blockchain.Tools.MessageBean;
import com.groupproject.blockchain.Tools.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupproject.blockchain.bean.Block;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyServer extends WebSocketServer {

    private int port;
    private Map<String,WebSocket> clientMap = new HashMap<String,WebSocket>();

    public MyServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(conn);
        System.out.println(conn.getRemoteSocketAddress()  + "close");
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("receive message from" + conn.getRemoteSocketAddress());
        try {
            //1. transfer message to bean
            ObjectMapper objectMapper = new ObjectMapper();
            MessageBean messageBean = objectMapper.readValue(message, MessageBean.class);
            //2. According to the bean.type do different transfer job
            //  1 -- > Transaction
            //  2 -- > 区块
            Collection<WebSocket> connections = getConnections();
            if(messageBean.type == 1){
                System.out.println(messageBean.msg);
/*
                Transaction transaction = objectMapper.readValue(messageBean.msg, Transaction.class);
*/
                    for (WebSocket connection : connections) {
                        if(!connection.getRemoteSocketAddress().toString().equals(conn.getRemoteSocketAddress().toString())){
                            connection.send(message);
                        }
                    }

            }
            else if(messageBean.type == 2){
                //do the block job
                System.out.println(messageBean.msg);
/*
                Block block = objectMapper.readValue(messageBean.msg, Block.class);
*/

                for (WebSocket connection : connections) {
                    if(!connection.getRemoteSocketAddress().toString().equals(conn.getRemoteSocketAddress().toString())){
                        connection.send(message);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("webSocket server" + port + "error");
        System.out.println(ex);
    }

    @Override
    public void onStart() {
        System.out.println("webSocket server" + port + "start");
    }

    public void startServer() {
        new Thread(this).start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket.getRemoteSocketAddress() + "connect successfully");
        System.out.println("open the server");

    }

    public static void main(String[] args) {
        new MyServer(8082).startServer();
    }
}
