package com.webank.webase.front.base.config;

import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.version.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangyang
 * @version 1.0
 * @project WeBASE-Sign
 * @description
 * @date 2023/9/21 18:06:02
 */
@Slf4j
public class NodeReqInterceptor implements HandlerInterceptor {
    @Autowired
    private KeyStoreService keyStoreService;

    @Autowired
    private VersionService versionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取由basemgr带过来的sa-token信息，存储到需要调用sign服务的service中

        String tokenKey = request.getHeader("tokenKey");
        String tokenValue = request.getHeader("tokenValue");

        log.info("%%%%%%%%% requtest, tokenKey: {}", tokenKey);
        log.info("%%%%%%%%% requtest, tokenValue: {}", tokenValue);

        if (!StringUtils.isEmpty(tokenKey) && !StringUtils.isEmpty(tokenValue)) {
            keyStoreService.setTokenKey(tokenKey);
            keyStoreService.setTokenValue(tokenValue);

            versionService.setTokenKey(tokenKey);
            versionService.setTokenValue(tokenValue);
        }

        return true;//返回 true 表示继续执行，返回 false 表示请求结束，不再继续执行
    }
}