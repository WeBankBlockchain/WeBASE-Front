/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.front.cert;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NodeCrtTest {
    private static final String flag = "-----" ;
    private static final String head = "-----BEGIN CERTIFICATE-----\n" ;
    private static final String tail = "-----END CERTIFICATE-----\n" ;

    @Test
    public void getChainCrt() throws IOException {
        InputStream inputStream = new ClassPathResource("conf/ca.crt").getInputStream();
        String str = getString(inputStream);
        String[] strArray = str.split(head); // 一个是空，一个是去除了head的string
        String ca = "";
        for(int i = 0; i < strArray.length; i++) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
            String[] strArray2 = strArray[i].split(tail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
            for(int j = 0; j < strArray2.length; j++) {
                ca = strArray2[j];
                if(ca.length() != 0) {
                    ca = formatStr(ca);
                }
            }
        }
        System.out.println(ca);
        Assert.assertNull(ca);
    }

    @Test
    public void getAgencyCrt() throws IOException {
        InputStream inputStream = new ClassPathResource("").getInputStream();
        String str = getString(inputStream);
        String[] strArray = str.split(head); // 一个是空，一个是去除了head的string
        for(int i = 0; i < strArray.length; i++) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
//            System.out.println("i: " + i + strArray[i]);
            String[] strArray2 = strArray[i].split(tail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
            for(int j = 0; j < strArray2.length; j++) {
                System.out.println("i: " + i + " j: " + j + " " + strArray2[j]);
                Assert.assertNull(strArray2[j]);
            }
        }
    }

    @Test
    public void getNodeCrt() throws IOException {
        List<String> list = new ArrayList<>();
        InputStream nodeCrtInput = new ClassPathResource("node.crt").getInputStream();
        String nodeCrtStr = getString(nodeCrtInput);

        String[] nodeCrtStrArray = nodeCrtStr.split(head);
        for(int i = 0; i < nodeCrtStrArray.length; i++) {
            String[] nodeCrtStrArray2 = nodeCrtStrArray[i].split(tail);
            for(int j = 0; j < nodeCrtStrArray2.length; j++) {
                String ca = nodeCrtStrArray2[j];
                if(ca.length() != 0 ) {
                    list.add(formatStr(ca));
                }
            }
        }
        System.out.println(list.get(0));
        Assert.assertNull(list.get(0));
        System.out.println(list.get(1));
        Assert.assertNull(list.get(1));
    }

    public String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }

    public void getNodeCrtStr() throws IOException{
        InputStream inputStream = new ClassPathResource("node.crt").getInputStream();
        String str = getString(inputStream);
        String[] strArray = str.split(flag);
        System.out.println(strArray[6]);
    }
    public void getAgencyCrtStr() throws IOException{
        InputStream inputStream = new ClassPathResource("node.crt").getInputStream();
        String str = getString(inputStream);
        String[] strArray = str.split(flag);
        System.out.println(strArray[6]);
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
