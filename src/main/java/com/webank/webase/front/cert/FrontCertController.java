package com.webank.webase.front.cert;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "/cert", tags = "cert manage interface")
@Slf4j
@RestController
@RequestMapping(value = "cert")
public class FrontCertController {
    @Autowired
    FrontCertService certService;

    @GetMapping("")
    public Object getFrontCerts() throws Exception {
        List<String> certList = new ArrayList<>();
        // node.crt = node +agency
        certList = certService.getNodeCerts();
        Map<String, String> map = new HashMap<>();
        map.put("node", certList.get(0));
        map.put("agency", certList.get(1));
        String chainCert = certService.getChainCrt();
        map.put("chain", chainCert);
        return map;
    }
}
