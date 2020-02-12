package com.webank.webase.front.transaction.websocket;


import com.webank.webase.front.base.properties.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

@Data
@Slf4j
@Configuration
public class WebSocketServiceConfig {

    @Autowired
    Constants constants;

    public static String url ;
    @Bean
    public  WebSocketService getWebSocketService() throws URISyntaxException, ConnectException {
        if (constants.getWebsocket().intValue()==1) {
            String url = String.format(Constants.WEBASE_SIGN_URI_WEBSOCKET, constants.getKeyServer());
            url = url + Constants.frontId;
            this.url = url;
            WebSocketClient ws = new WebSocketClient(new URI(url));
            WebSocketService webSocketService = new WebSocketService(ws, false);
            webSocketService.connect();
            log.info("websocket connect to {}  successfully", url);
            return webSocketService;
        }
            return null;

    }
}
