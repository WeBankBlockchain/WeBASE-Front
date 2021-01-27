/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.front.util;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;

import java.util.ArrayList;
import java.util.List;

public class PemUtils {
    public static final String crtContentHeadNoLF = "-----BEGIN PRIVATE KEY-----" ;
    public static final String crtContentHead = "-----BEGIN PRIVATE KEY-----\n" ;
    public static final String crtContentTail = "-----END PRIVATE KEY-----\n" ;
    public static final String crtTailForConcat = "\n-----END PRIVATE KEY-----\n" ;

    /**
     * get pem file's content without head and tail
     * support importing multiple pem private key
     * @param certContent
     * @return
     */
    public static List<String> getPemBareContentList(String certContent) {
        List<String> list = new ArrayList<>();
        if(!certContent.startsWith(crtContentHeadNoLF)){
            throw new FrontException(ConstantCode.PEM_FORMAT_ERROR);
        }
        String[] nodeCrtStrArray = certContent.split(crtContentHead);
        for(int i = 0; i < nodeCrtStrArray.length; i++) {
            String[] nodeCrtStrArray2 = nodeCrtStrArray[i].split(crtContentTail);
            for(int j = 0; j < nodeCrtStrArray2.length; j++) {
                String ca = nodeCrtStrArray2[j];
                if(ca.length() != 0) {
                    list.add(formatPemStr(ca));
                }
            }
        }
        return list;
    }

    public static String formatPemStr(String string) {
        return string.substring(0, string.length() - 1);
    }

    /**
     * 给pem的内容加上头和尾
     * begin ...
     * end ...
     */
    public static String addPemHeadAndTail(String certContent) {
        String headToConcat = crtContentHead;
        String fullCert = headToConcat.concat(certContent).concat(crtTailForConcat);
        return fullCert;
    }
}
