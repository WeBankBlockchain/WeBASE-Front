/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.base;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AesUtils {

    @Autowired
    public Constants constants;

    /**
     * Encrypt by aes.
     */
    public String aesEncrypt(String content, String key) {
        if (StringUtils.isBlank(key) || key.length() != 16) {
            log.warn("aesEncrypt. error key,use default key:{}", constants.getAesKey());
            key = constants.getAesKey();
        }

        try {
            byte[] keyBytes = key.getBytes("UTF-8");
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(content.getBytes("utf-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            log.warn("fail aesEncrypt", ex);
            return null;
        }
    }

    /**
     * Encrypt by aes.
     */
    public String aesEncrypt(String content) {
        return aesEncrypt(content, constants.getAesKey());
    }

    /**
     * Decrypt by aes.
     */
    public String aesDecrypt(String content, String key) {
        if (StringUtils.isBlank(key) || key.length() != 16) {
            log.warn("aesDecrypt. error key,use default key:{}", constants.getAesKey());
            key = constants.getAesKey();
        }
        try {
            byte[] keyBytes = key.getBytes("UTF-8");
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] encrypted1 = Base64.getDecoder().decode(content);
            byte[] original = cipher.doFinal(encrypted1);

            return new String(original, "UTF-8");

        } catch (Exception ex) {
            log.warn("fail aesDecrypt", ex);
            return null;
        }
    }

    /**
     * Decrypt by aes.
     */
    public String aesDecrypt(String content) {
        return aesDecrypt(content, constants.getAesKey());
    }
}
