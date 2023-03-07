/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.front.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * @author marsli
 */
@Slf4j
public class ZipUtils {

    /**
     * @param path   要压缩的文件路径
     * @param outputDir zip包的生成目录，默认为tempZip
     * @param dirInZip  directory in zip file, if blank, no dir in zip
     */
    public static void generateZipFile(String path, String outputDir, String dirInZip, String zipFileName) throws Exception {

        File file2Zip = new File(CleanPathUtil.cleanString(path));
        // 压缩文件的路径不存在
        if (!file2Zip.exists()) {
            log.error("file not exist:{}", path);
            throw new Exception("file not exist: " + path);
        }
        // 用于存放压缩文件的文件夹
        File compress = new File(outputDir);
        // 如果文件夹不存在，进行创建
        if (!compress.exists() ){
            compress.mkdirs();
        }
        // 目的压缩文件，已存在则先删除
        // tempZip/conf.zip
        String generateFileName = CleanPathUtil.cleanString(compress.getAbsolutePath() + File.separator + zipFileName);
        File confZip = new File(generateFileName);
        if (confZip.exists() ) {
            log.info("confZip exist, now delete:{}", confZip);
            boolean result = confZip.delete();
            log.info("confZip exist, delete result:{}", result);
        }
        // 输出流
        FileOutputStream outputStream = new FileOutputStream(generateFileName);
        // 压缩输出流
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
        // 传入输出流，传入需要压缩的file路径
        generateFile(zipOutputStream, file2Zip, dirInZip);

        log.info("file2Zip:{} and outputFile:{}" ,file2Zip.getAbsolutePath(), generateFileName);
        // 关闭 输出流
        zipOutputStream.close();
        outputStream.close();
    }


    /**
     * @param out  输出流
     * @param file 目标文件
     * @param dir  在压缩包中的文件夹
     * @throws Exception
     */
    private static void generateFile(ZipOutputStream out, File file, String dir) throws Exception {

        // 当前的是文件夹，则进行一步处理
        if (file.isDirectory()) {
            //得到文件列表信息
            File[] files = file.listFiles();
            //将文件夹添加到下一级打包目录
            out.putNextEntry(new ZipEntry(dir + "/"));

            dir = dir.length() == 0 ? "" : dir + "/";

            //循环将文件夹中的文件打包
            for (int i = 0; i < files.length; i++) {
                generateFile(out, files[i], dir + files[i].getName());
            }

        } else { // 当前是文件

            // 输入流
            FileInputStream inputStream = new FileInputStream(file);
            // 标记要打包的条目
            out.putNextEntry(new ZipEntry(dir));
            // 进行写操作
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) > 0) {
                out.write(bytes, 0, len);
            }
            // 关闭输入流
            inputStream.close();
        }
    }
}
