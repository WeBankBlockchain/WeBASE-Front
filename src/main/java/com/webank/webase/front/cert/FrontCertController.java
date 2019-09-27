package com.webank.webase.front.cert;

import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
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

//    @GetMapping("chain")
//    public String getChainCrt() throws Exception {
//        String chainCrtStr = getFirstCrt(getString(crtStream));
//        return formatStr(chainCrtStr);
//    }
//
//    @GetMapping("agency")
//    public String getAgencyCrt() throws Exception {
//
//        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
//        // 只读取后面那段String
//        String agencyCrtStr = getSecondCrt(getString(crtStream));
//        return formatStr(agencyCrtStr);
//    }
//
//    @GetMapping("node")
//    public String getNodeCrt() throws Exception {
//        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
//        // 只读取前面那段String
//        String nodeCrtStr = getFirstCrt(getString(crtStream));
//        return formatStr(nodeCrtStr);
//    }
}
