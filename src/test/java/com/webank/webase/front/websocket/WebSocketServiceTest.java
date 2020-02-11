package com.webank.webase.front.websocket;

import com.webank.webase.front.transaction.websocket.SignMessage;
import com.webank.webase.front.transaction.websocket.WebSocketClient;
import com.webank.webase.front.transaction.websocket.WebSocketService;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.mock;


public class WebSocketServiceTest {

    private static final int REQUEST_ID = 1;
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://localhost:5004/websocket/1"));
    private ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);

//    private WebSocketListener listener = new WebSocketListener() {
//        @Override
//        public void onMessage(String message) throws IOException {
//            onWebSocketMessage(message);
//        }
//
//        @Override
//        public void onError(Exception e) {
//            System.out.println("Received error from a WebSocket connection");
//        }
//
//        @Override
//        public void onClose() {
//            System.out.println("close");
//        }
//    };

    private  WebSocketService  webSocketService  =  new WebSocketService(webSocketClient,true);

    private Request<?, SignMessage> request = new Request<>(
                "sign&1",
                Arrays.asList("100001","0xdd"),
                webSocketService,
               SignMessage.class);

    public WebSocketServiceTest() throws URISyntaxException {
    }

    @Before
    public void before() throws InterruptedException, ConnectException {

      //  webSocketClient.setListener(listener);
//        webSocketClient.connect();
//        while (!webSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
//            System.out.println("连接中···请稍后");
//        }
        webSocketService.connect();
        request.setId(2);
    }

    @Test
    public void testWaitingForReplyToSentRequest() throws Exception {

        webSocketService.sendAsync(request, SignMessage.class);

     //   System.out.println(ethSign.getSignature());
        System.out.println(  webSocketService.isWaitingForReply(request.getId()));
        System.out.println("end");
        Thread.sleep(1000);
    }


}