package com.webank.webase.front.precompiledapi.sysconf;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.keystore.KeyStoreService;
import com.webank.webase.front.precompiledapi.sysconf.entity.SystemConfig;
import com.webank.webase.front.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.common.PrecompiledResponse;
import org.fisco.bcos.web3j.precompile.config.SystemConfigService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PrecompiledSysConfigService {

    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private SystemConfigRepository systemConfigRepository;

    // 根据前台传的user address获取私钥
    public Credentials getCredentials(String fromAddress) throws Exception {
        return keyStoreService.getCredentials(fromAddress, false);
    }
    public Credentials getCredentialsForQuery() throws Exception {
//        KeyStoreInfo keyStoreInfo = keyStoreService.createKeyStore(false, KeyTypes.LOCALRANDOM.getValue(), "");
//        return Credentials.create(keyStoreInfo.getPrivateKey());
        PEMManager pemManager = new PEMManager();
        InputStream pemStream = new ClassPathResource(Constants.account1Path).getInputStream();
        pemManager.load(pemStream);
        return GenCredential.create(pemManager.getECKeyPair().getPrivateKey().toString(16));
    }

    /**
     * System config related
     * 启动项目时，检查是否已有table，
     * 否则Create table sysconfig(groupId, from key, value)
     */
    public Object setSysConfigValueByKey(SystemConfig systemConfig) throws Exception {
        int groupId = systemConfig.getGroupId();
        String fromAddress = systemConfig.getFromAddress();
        String key = systemConfig.getConfigKey();
        String value = systemConfig.getConfigValue();
        // for undo
        String oldValue = getSysConfigByKey(groupId, key);

        // check system value
        if(PrecompiledUtils.TxGasLimit.equals(key)) {
            if (Integer.valueOf(value) < PrecompiledUtils.TxGasLimitMin) {
                return ConstantCode.FAIL_SET_SYSTEM_CONFIG_TOO_SMALL;
            }
        }
        SystemConfigService systemConfigService = new SystemConfigService(
                web3jMap.get(groupId), getCredentials(fromAddress));
        try{
            // @param result {"code":0,"msg":"success"}
            String result = systemConfigService.setValueByKey(key, value);
            Map<String, Object> resMapForCheck;
            ObjectMapper mapper = new ObjectMapper();
            resMapForCheck = mapper.readValue(result, Map.class);

            if(resMapForCheck.get("msg").toString().equals("success")){  // set成功了再存到db
                // 如果有记录，则update，如果没, 则insert
                // save失败会抛出FrontException
                SystemConfig saveResult = saveSysConfig(systemConfig);

                return result;//透传
            }else {
                return ConstantCode.FAIL_SET_SYSTEM_CONFIG;
            }
        }catch (JsonParseException | JsonMappingException e) {
            // parse失败回滚, save2Db失败回滚
            systemConfigService.setValueByKey(key, oldValue);
            throw new FrontException(PrecompiledUtils.CRUD_SQL_ERROR,
                    "Could not parse string to map." + e.getMessage());
        }

    }

    public List<SystemConfig> querySysConfigByGroupId(int groupId) throws Exception {

        List<SystemConfig> list = getConfigListFromDb(groupId);
        // first time initialize db
        if(list.size() == 0) {
            initSysConfigDb(groupId);
            list = getConfigListFromDb(groupId);
        }
        // if sync with chain, update list
        if(syncSysConfigDb(groupId) > 0) {
            list = getConfigListFromDb(groupId);
        }

        return list;
    }

    public String getSysConfigByKey(int groupId, String key) throws Exception {
        // 校验
        String result = web3jMap.get(groupId).getSystemConfigByKey(key).sendForReturnString();
        return result;

    }

    public void initSysConfigDb(int groupId) throws Exception{
        // 再次确认db的system config为空
        List<SystemConfig> check = systemConfigRepository.findByGroupId(groupId);
        if(check.size() == 0) {
            String txCountLimitValue = web3jMap.get(groupId).
                    getSystemConfigByKey(PrecompiledUtils.TxCountLimit).sendForReturnString();

            String txGasLimitValue = web3jMap.get(groupId).
                    getSystemConfigByKey(PrecompiledUtils.TxGasLimit).sendForReturnString();

            // 初始化数据库 tx_count_limit init from chain
            SystemConfig systemConfigCount = new SystemConfig();
            systemConfigCount.setId(Long.valueOf("1"));
            systemConfigCount.setGroupId(groupId);
            systemConfigCount.setFromAddress("0x0");
            systemConfigCount.setConfigKey(PrecompiledUtils.TxCountLimit);
            systemConfigCount.setConfigValue(txCountLimitValue);
            newSystemConfig(systemConfigCount);// new

            // tx_gas_limit init from chain
            SystemConfig systemConfigGas = new SystemConfig();
            systemConfigGas.setId(Long.valueOf("2"));
            systemConfigGas.setGroupId(groupId);
            systemConfigGas.setFromAddress("0x0");
            systemConfigGas.setConfigKey(PrecompiledUtils.TxGasLimit);
            systemConfigGas.setConfigValue(txGasLimitValue);
            newSystemConfig(systemConfigGas);// new
        }
    }

    // sync db's system config value with chain's config value
    public int syncSysConfigDb(int groupId) throws Exception{
        int syncNum = 0;
        String txCountLimitValue = web3jMap.get(groupId).
                getSystemConfigByKey(PrecompiledUtils.TxCountLimit).sendForReturnString();
        String txGasLimitValue = web3jMap.get(groupId).
                getSystemConfigByKey(PrecompiledUtils.TxGasLimit).sendForReturnString();

        SystemConfig systemConfigCount = systemConfigRepository.findByGroupIdAndConfigKey(groupId, PrecompiledUtils.TxCountLimit);
        SystemConfig systemConfigGas = systemConfigRepository.findByGroupIdAndConfigKey(groupId, PrecompiledUtils.TxGasLimit);

        if(systemConfigCount.getConfigValue() != txCountLimitValue) {
            // tx_count_limit synchronize from chain
            systemConfigCount.setConfigValue(txCountLimitValue);
            // 要有主键id才能保证update，否则会当作新的直接insert
            systemConfigCount.setId(systemConfigCount.getId());
            systemConfigRepository.save(systemConfigCount);
            syncNum++;
        }
        if(systemConfigGas.getConfigValue() != txGasLimitValue) {
            // tx_gas_limit synchronize from chain
            systemConfigGas.setConfigValue(txGasLimitValue);
            systemConfigGas.setId(systemConfigGas.getId());
            systemConfigRepository.save(systemConfigGas);
            syncNum++;
        }
        return syncNum;
    }

    /**
     * save config data.
     */
    public SystemConfig saveSysConfig(SystemConfig systemConfig) {
        log.debug("start saveContract contractReq:{}", JSON.toJSONString(systemConfig));
        int groupId = systemConfig.getGroupId();
        String configKey = systemConfig.getConfigKey();

        SystemConfig check = getByConfigKeyFromDb(groupId, configKey);
        if (Objects.isNull(check)) { // if key exist, update
            return newSystemConfig(systemConfig);// new
        } else {
            // 要有主键id才能保证update，否则会当作新的直接insert
            systemConfig.setId(check.getId());
            return updateSystemConfig(systemConfig);// update
        }
    }
    /**
     * save new system config.
     */
    private SystemConfig newSystemConfig(SystemConfig systemConfig) {
        systemConfigRepository.save(systemConfig);
        return systemConfig;
    }

    /**
     * update system config.
     */
    private SystemConfig updateSystemConfig(SystemConfig systemConfig) {
        // 确保exist后, update
        systemConfigRepository.save(systemConfig);
        return systemConfig;
    }

    private List<SystemConfig> getConfigListFromDb(int groupId) {
        List<SystemConfig> list = systemConfigRepository.findByGroupId(groupId);

        return list;
    }
    /**
     * verify config key not exist.
     * else cannot new config data
     */


    private SystemConfig getByConfigKeyFromDb(int groupId, String configKey) {
        SystemConfig systemConfig = systemConfigRepository.findByGroupIdAndConfigKey(groupId, configKey);
        return systemConfig;
    }

//    private void verifyConfigKeyNotExist(int groupId, String configKey) {
//        SystemConfig systemConfig =
//                systemConfigRepository.findByGroupIdAndConfigKey(groupId, configKey);
//        if (Objects.nonNull(systemConfig)) {
//            log.warn("system config exists. groupId:{} configKey:{}", groupId, configKey);
//            throw new FrontException(ConstantCode.SYSTEM_CONFIG_EXIST);
//        }
//    }


//    private SystemConfig verifyConfigKeyExist(int groupId, String configKey) {
//        // 如果有多个相同的就会报错
//        SystemConfig systemConfig = systemConfigRepository.findByGroupIdAndConfigKey(groupId, configKey);
//        if (Objects.isNull(systemConfig)) {
//            log.info("system config key is invalid. configKey:{}", configKey);
//            throw new FrontException(ConstantCode.INVALID_SYSTEM_CONFIG_KEY);
//        }
//        return systemConfig;
//    }


}
