package com.webank.webase.front.config;

import lombok.Data;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "channelService")
public class ServiceConfig {

    private String orgID;
    private Integer connectSeconds= 30 ;
    private Integer connectSleepPerMillis = 1;
    private GroupChannelConnectionsConfig allChannelConnections;
    private static int groupId =1;

    @Bean
    public Service getService(GroupChannelConnectionsConfig groupChannelConnectionsConfig) {
        Service channelService= new Service();
        channelService.setConnectSeconds(connectSeconds);
        channelService.setOrgID(orgID);
        channelService.setConnectSleepPerMillis(connectSleepPerMillis);
        channelService.setGroupId(groupId);
        channelService.setAllChannelConnections(groupChannelConnectionsConfig);
        return channelService;
    }
}
