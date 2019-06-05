package com.webank.webase.front.keystore;

import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping(value = "/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;

    @ApiOperation(value = "get PrivateKey", notes = "get PrivateKey")
    @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean")
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo getPrivateKey(boolean useAes) {
        return keyStoreService.createPrivateKey(useAes);
    }

    @ApiOperation(value = "import PrivateKey", notes = "import PrivateKey")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "privateKey", value = "private key", required = true, dataType = "String"),
        @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean")
    })
    @RequestMapping(method = RequestMethod.GET, value = "/import")
    public KeyStoreInfo importPrivateKey(String privateKey,
        @RequestParam(required = false, defaultValue = "true") boolean useAes) {
        return keyStoreService.getKeyStoreFromPrivateKey(privateKey, useAes);
    }
}
