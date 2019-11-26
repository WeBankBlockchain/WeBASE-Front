package com.webank.webase.front.base.config;

import lombok.Data;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "encrypt-type")
public class EncryptTypeConfig {
    // 0:standard, 1:guomi
    private int encryptType;

    /**
     * 覆盖构造函数，不能写getEncrytType()
     * @return
     */
    @Bean
    public EncryptType EncryptType() {
        return new EncryptType(encryptType);
    }

}
