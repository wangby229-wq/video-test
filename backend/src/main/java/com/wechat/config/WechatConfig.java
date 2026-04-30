package com.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.corp")
public class WechatConfig {
    private String id;
    private Agent agent;
    
    @Data
    public static class Agent {
        private String id;
        private String secret;
    }
    
    private String tokenUrl;
    private String userInfoUrl;
    private String userDetailUrl;
    private String oauthUrl;
    private String qrConnectUrl;
    
    private String mediaUrl;
    private String sendMessageUrl;
}
