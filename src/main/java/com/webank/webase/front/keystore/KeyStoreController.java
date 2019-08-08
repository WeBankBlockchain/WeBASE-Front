package com.webank.webase.front.keystore;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.enums.KeyTypes;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;

    @ApiOperation(value = "getKeyStore", notes = "get key store info")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean"),
        @ApiImplicitParam(name = "type", value = "private key type", dataType = "int"),
        @ApiImplicitParam(name = "userName", value = "user name", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo getKeyStore(@RequestParam(required = true) boolean useAes, 
            @RequestParam(required = false, defaultValue = "2") int type,
            @RequestParam(required = false) String userName) {
        return keyStoreService.createKeyStore(useAes, type, userName);
    }

    @ApiOperation(value = "import PrivateKey", notes = "import PrivateKey")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "privateKey", value = "private key", required = true, dataType = "String"),
        @ApiImplicitParam(name = "userName", value = "user name", required = true, dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET, value = "/import")
    public KeyStoreInfo importPrivateKey(@RequestParam(required = true) String privateKey,
        @RequestParam(required = true) String userName) {
        return keyStoreService.getKeyStoreFromPrivateKey(privateKey, false, KeyTypes.LOCALUSER.getValue(), userName);
    }
    
    @ApiOperation(value = "getKeyStores", notes = "get local KeyStore lists")
    @RequestMapping(method = RequestMethod.GET, value = "/localKeyStores")
    public List<KeyStoreInfo> getLocalKeyStores() {
        log.info("start getLocalKeyStores.");
        return keyStoreService.getLocalKeyStores();
    }
    
    @ApiOperation(value = "delete", notes = "delete local KeyStore by address")
    @ApiImplicitParam(name = "address", value = "user address", required = true, dataType = "String")
    @DeleteMapping("/{address}")
    public BaseResponse deleteKeyStore(@PathVariable String address) {
        log.info("start deleteKeyStores. address:{}", address);
        keyStoreService.deleteKeyStore(address);
        return new BaseResponse(ConstantCode.RET_SUCCEED);
    }
}
