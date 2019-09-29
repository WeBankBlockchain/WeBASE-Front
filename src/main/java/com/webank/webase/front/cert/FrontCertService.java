package com.webank.webase.front.cert;


import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FrontCertService {
    private final static String flag = "-----" ;
    private final static String head = "-----BEGIN CERTIFICATE-----\n" ;
    private final static String tail = "-----END CERTIFICATE-----\n" ;
    private final static String nodeCrt = "/node.crt";
    private final static String caCrt = "/ca.crt";
    @Autowired
    Constants constants;
    /**
     * 设置了front对应的节点的目录，如/data/fisco/nodes/127.0.0.1/node0
     * 则获取 ${path}/conf 中的ca.crt, node.crt
     * @return List<String> 或者 String
     */
    // 0 is node ca, 1 is agency ca
    public List<String> getNodeCerts() {
        List<String> list = new ArrayList<>();
        String nodeCertPath = constants.getNodeCertPath();
        nodeCertPath = nodeCertPath.concat(Constants.NODE_CERT_PATH_CONF_DIR);
        try {
            InputStream nodeCrtInput = new FileInputStream(nodeCertPath.concat(nodeCrt));
            String nodeCrtStr = getString(nodeCrtInput);
            String[] nodeCrtStrArray = nodeCrtStr.split(head);
            for(int i = 0; i < nodeCrtStrArray.length; i++) {
                String[] nodeCrtStrArray2 = nodeCrtStrArray[i].split(tail);
                for(int j = 0; j < nodeCrtStrArray2.length; j++) {
                    String ca = nodeCrtStrArray2[j];
                    if(ca.length() != 0) {
                        list.add(formatStr(ca));
                    }
                }
            }
        }catch (IOException e) {
            log.error("FrontCertService getCerts, node cert(node.crt) path prefix error");
            throw new FrontException("FileNotFound, node cert(node.crt) path prefix error");
        }
        return list;
    }

    public String getChainCrt() {
        String chainCertPath = constants.getNodeCertPath();
        chainCertPath = chainCertPath.concat(Constants.NODE_CERT_PATH_CONF_DIR);
        String ca = "";
        try{
            InputStream caInput = new FileInputStream(chainCertPath.concat(caCrt));
            String caStr = getString(caInput);
            String[] caStrArray = caStr.split(head); // 一个是空，一个是去除了head的string
            for(int i = 0; i < caStrArray.length; i++) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
                String[] caStrArray2 = caStrArray[i].split(tail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
                for(int j = 0; j < caStrArray2.length; j++) {
                    ca = caStrArray2[j];
                    if(ca.length()  != 0) {
                        ca = formatStr(ca);
                    }
                }
            }
        }catch (IOException e) {
            log.error("FrontCertService getCerts, chain cert(ca.crt) path prefix error");
            throw new FrontException("FileNotFound, chain cert(ca.crt) path error");
        }
        return ca;
    }


    public String getString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[0];
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String str = new String(bytes);
        return str;
    }


    public String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
}
