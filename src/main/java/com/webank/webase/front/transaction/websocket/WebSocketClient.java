package com.webank.webase.front.transaction.websocket;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.protocol.websocket.WebSocketListener;
import org.java_websocket.handshake.ServerHandshake;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class WebSocketClient extends org.java_websocket.client.WebSocketClient{
    private Optional<WebSocketListener> listenerOpt = Optional.empty();

    public WebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Opened WebSocket connection to {}", uri);
    }

    @OnMessage
    public void onMessage(String s) {
        log.debug("Received message {} from server {}", s, uri);
        listenerOpt.ifPresent(listener -> {
            try {
                listener.onMessage(s);
            } catch (Exception e) {
                log.error("Failed to process message '{}' from server {}", s, uri, e);
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Closed WebSocket connection to {}, because of reason: '{}'."
                + "Connection closed remotely: {}", uri, reason, remote);
        listenerOpt.ifPresent(WebSocketListener::onClose);
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket connection to {} failed with error", uri, ex);
        listenerOpt.ifPresent(listener -> listener.onError(ex));

    }

    public void setListener(WebSocketListener listener) {
        this.listenerOpt = Optional.ofNullable(listener);
    }

}
