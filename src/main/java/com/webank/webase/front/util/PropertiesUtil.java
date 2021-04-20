package com.webank.webase.front.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author marsli
 */
@Slf4j
public class PropertiesUtil {

    /**
     * 获取Properties对象
     *
     * @return
     */
    public static Properties getProperties() {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            //data.properties在resources目录下
            inputStream = PropertiesUtil.class.getClassLoader()
                .getResourceAsStream("data.properties");
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            log.error("data.properties文件未找到!");
        } catch (IOException e) {
            log.error("出现IOException");
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("data.properties文件流关闭出现异常");
            }
        }
        return properties;
    }

    /**
     * 根据key查询value值
     *
     * @param key key
     * @return
     */
    public static String getValue(String key) {
        Properties properties = getProperties();
        String value = properties.getProperty(key);
        return value;
    }

    /**
     * 新增/修改数据
     *
     * @param key
     * @param value
     */
    public static void setValue(String key, String value) {
        Properties properties = getProperties();
        properties.setProperty(key, value);
        //此处获取的路径是target下classes

        //这里的path是项目文件的绝对路径
        //先获取项目绝对路径：Thread.currentThread().getContextClassLoader().getResource("").getPath();
        //然后在项目路径后面拼接"properties/sysConfig.properties";
        // 原注释
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        path = path + "data.properties";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(path);
            properties.store(fileOutputStream, "注释");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log.error("data.properties文件流关闭出现异常");
            }
        }
    }
}