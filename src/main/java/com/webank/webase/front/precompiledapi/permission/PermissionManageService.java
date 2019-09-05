package com.webank.webase.front.precompiledapi.permission;

import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.Constants;
import com.webank.webase.front.keystore.KeyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.fisco.bcos.web3j.precompile.permission.PermissionService;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PermissionManageService {
    @Autowired
    Map<Integer, Web3j> web3jMap;
    @Autowired
    private KeyStoreService keyStoreService;

//    // 获取credentials实例用于发起交易

    // 根据前台传的user address获取私钥
    public Credentials getCredentials(String fromAddress) throws Exception {
        return keyStoreService.getCredentials(fromAddress, false);
    }
    public Credentials getCredentialsForQuery() throws Exception {
        PEMManager pemManager = new PEMManager();
        InputStream pemStream = new ClassPathResource(Constants.account1Path).getInputStream();
        pemManager.load(pemStream);
        Credentials credentialsPEM = GenCredential.create(pemManager.getECKeyPair().getPrivateKey().toString(16));
        return credentialsPEM;
    }

    /**
     * 根据permission type 参数来分发请求
     */


    /**
     * manage chain admin related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantPermissionManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));
        return permissionService.grantPermissionManager(userAddress);
    }

    public String  revokePermissionManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokePermissionManager(userAddress);
    }

    /**
     * 查询PermissionManager 不需要发起交易
     */
    public List<PermissionInfo> listPermissionManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());

        return permissionService.listPermissionManager();
    }

    /**
     * manage deploy create Contract Manager related
     */
    public String grantDeployAndCreateManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantDeployAndCreateManager(userAddress);
    }

    public String revokeDeployAndCreateManager(int groupId, String fromAddress, String userAddress) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));
        return permissionService.revokeDeployAndCreateManager(userAddress);
    }

    public List<PermissionInfo> listDeployAndCreateManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listDeployAndCreateManager();
    }

    /**
     * manage userTableManager related
     * @param groupId
     * @param tableName
     * @param userAddress
     * @return
     * @throws Exception
     */
    public Object grantUserTableManager(int groupId, String fromAddress, String tableName, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantUserTableManager(tableName, userAddress);

    }

    public Object revokeUserTableManager(int groupId, String fromAddress, String tableName, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeUserTableManager(tableName, userAddress);
    }

    public List<PermissionInfo> listUserTableManager(int groupId, String tableName) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listUserTableManager(tableName);

    }

    /**
     * manage NodeManager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantNodeManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantNodeManager(userAddress);
    }

    public String revokeNodeManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeNodeManager(userAddress);
    }

    public List<PermissionInfo> listNodeManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listNodeManager();
    }

    /**
     * manage system config Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantSysConfigManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantSysConfigManager(userAddress);
    }

    public String revokeSysConfigManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeSysConfigManager(userAddress);
    }

    public List<PermissionInfo> listSysConfigManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listSysConfigManager();
    }

    /**
     * manage CNS Manager related
     * @param groupId
     * @param userAddress
     * @return
     * @throws Exception
     */
    public String grantCNSManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.grantCNSManager(userAddress);
    }

    public String revokeCNSManager(int groupId, String fromAddress, String userAddress) throws Exception {
        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentials(fromAddress));

        return permissionService.revokeCNSManager(userAddress);
    }

    public List<PermissionInfo> listCNSManager(int groupId) throws Exception {

        PermissionService permissionService = new PermissionService(web3jMap.get(groupId), getCredentialsForQuery());
        return permissionService.listCNSManager();
    }



}
