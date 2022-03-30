//package com.webank.webase.front.base.config;
//
//import com.webank.webase.front.base.properties.Constants;
//import org.apache.coyote.http11.Http11NioProtocol;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
//import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class TomcatConfig {
//
//    @Autowired
//    Constants constantProperties;
//    @Bean
//    public EmbeddedServletContainerFactory createEmbeddedServletContainerFactory() {
//        TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();
//            tomcatFactory.addConnectorCustomizers(connector -> {
//            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
//            protocol.setKeepAliveTimeout(constantProperties.getKeepAliveTimeout()* 1000);
//            protocol.setMaxKeepAliveRequests(constantProperties.getKeepAliveRequests());
//        });
//        return tomcatFactory;
//    }
//}
