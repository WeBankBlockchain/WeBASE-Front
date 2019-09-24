package com.webank.webase.front.cert;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;


@Api(value = "/cert", tags = "cert manage interface")
@Slf4j
@RestController
@RequestMapping(value = "cert")
public class FrontCertManage {
    public final static String flag = "-----";

    @GetMapping("chain")
    public String getChainCrt() throws Exception {
        InputStream crtStream = new ClassPathResource("ca.crt").getInputStream();
        String chainCrtStr = getFirstCrt(getString(crtStream));
        return formatStr(chainCrtStr);
    }

    @GetMapping("agency")
    public String getAgencyCrt() throws Exception {

        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
        // 只读取后面那段String
        String agencyCrtStr = getSecondCrt(getString(crtStream));
        return formatStr(agencyCrtStr);
    }

    @GetMapping("node")
    public String getNodeCrt() throws Exception {
        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
        // 只读取前面那段String
        String nodeCrtStr = getFirstCrt(getString(crtStream));
        return formatStr(nodeCrtStr);
    }

    public String getFirstCrt(String string) {
        String[] strArray = string.split(flag);
        return strArray[2];
    }

    public String getSecondCrt(String string) {
        String[] strArray = string.split(flag);
        return strArray[6];
    }

    public String getString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[0];
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String str = new String(bytes);
        return str;
    }

    public InputStream getStream(String string) throws IOException {
        InputStream is = new ByteArrayInputStream(string.getBytes());
        return is;
    }

    public String formatStr(String string) {
        return string.substring(1, string.length() - 1);
    }
}
