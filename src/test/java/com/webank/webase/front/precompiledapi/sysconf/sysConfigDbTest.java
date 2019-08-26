package com.webank.webase.front.precompiledapi.sysconf;

import com.webank.webase.front.channel.test.TestBase;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class sysConfigDbTest extends TestBase {


    public static ApplicationContext context = null;
    public static String key;
    public static String value;

    /**
     * System config related
     * TODO 启动项目时，检查是否已有table，否则Create table sysconfig(groupId, key, value)
     */
    @Test
    public void setSysConfigValueByKey() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        PEMManager pem = context.getBean(PEMManager.class);
        ECKeyPair pemKeyPair = pem.getECKeyPair();
        //链管理员私钥加载
        Credentials credentialsPEM = GenCredential.create(pemKeyPair.getPrivateKey().toString(16));
        SystemConfigService systemConfigService = new SystemConfigService(web3j, credentialsPEM);

        // TODO 存到数据库 如果有记录，则update，如果没则insert
        String insertSysConfig = "insert ";
        systemConfigService.setValueByKey(key, value);

    }

    public Object querySysConfigByGroupId(int groupId) throws Exception {
//        String key = "tx_count_limit";
//        String key2 = "tx_gas_limit"; // TODO 从数据库获取系统配置的key, value
        String queryByGroupId = "select * from sysconfig";
        return "";
    }
}
