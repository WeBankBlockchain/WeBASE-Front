package com.webank.webase.front.cert;

import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
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
        String chainCertStr;
        String nodeCrtStr;
        String agencyCrtStr;
        // node.crt = node +agency
        try {
            certList = certService.getNodeCerts();
            chainCertStr = certService.getChainCrt();
        }catch (FrontException e) {
            return new BaseResponse(ConstantCode.CERT_FILE_NOT_FOUND, e.getMessage());
        }
        Map<String, String> map = new HashMap<>();
        nodeCrtStr = certList.get(0);
        agencyCrtStr = certList.get(1);
        if(checkCertStrNonNull(nodeCrtStr)) {
            map.put("node", nodeCrtStr);
        }
        if(checkCertStrNonNull(agencyCrtStr)) {
            map.put("agency", agencyCrtStr);
        }
        if(checkCertStrNonNull(chainCertStr)) {
            map.put("chain", chainCertStr);
        }
        return map;
    }

    private boolean checkCertStrNonNull(String certStr) {
        if(certStr != "" || certStr != null) {
            return true;
        } else {
            return false;
        }
    }
}
