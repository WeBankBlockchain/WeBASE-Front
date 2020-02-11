package com.webank.webase.front.websocket;

import com.webank.webase.front.transaction.websocket.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.websocket.WebSocketListener;
import org.java_websocket.WebSocket;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.*;

@Slf4j
public class WebSocketClientTest {

    private WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onMessage(String message) throws IOException {
            System.out.println(message);
        }

        @Override
        public void onError(Exception e) {
            System.out.println("Received error from a WebSocket connection");
        }

        @Override
        public void onClose() {
            System.out.println("close");
        }
    };

    private WebSocketClient client;

    @Before
    public void before() throws Exception {
        client = new WebSocketClient(new URI("ws://localhost:5004/websocket/1"));

        client.setListener(listener);
        client.connect();
        while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            System.out.println("连接中···请稍后");
        }
    }

    @Test
    public void testSendMessage() throws Exception {

            client.send("{ \"messageId\":\"1\", \"encodedDataStr\":\"0xeeee\", \"userId\": 100001, \"frontId\":\"1\" }");

            listener.onMessage("message");
            Thread.sleep(1000);
    }


}