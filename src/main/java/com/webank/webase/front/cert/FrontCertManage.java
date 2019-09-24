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

    @GetMapping("chain")
    public InputStream getChainCrt() throws Exception {
        InputStream crtStream = new ClassPathResource("ca.crt").getInputStream();
        return crtStream;
    }

    @GetMapping("agency")
    public InputStream getAgencyCrt() throws Exception {
        // 只读取后面那段String
        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
        String agency = getString(crtStream);

        return crtStream;
    }

    @GetMapping("node")
    public InputStream getNodeCrt() throws Exception {
        // 只读取前面那段String
        InputStream crtStream = new ClassPathResource("node.crt").getInputStream();
        return crtStream;
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
}
