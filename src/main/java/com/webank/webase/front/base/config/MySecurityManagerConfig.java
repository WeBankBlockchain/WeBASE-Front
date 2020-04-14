package com.webank.webase.front.base.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySecurityManagerConfig {

    public static void forbidSystemExitCall() {
        // Set current security manager.
        // If the argument is null and no security manager has been established then no action is taken
        System.setSecurityManager(new NoExitSecurityManager());
    }

    public static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }

}
