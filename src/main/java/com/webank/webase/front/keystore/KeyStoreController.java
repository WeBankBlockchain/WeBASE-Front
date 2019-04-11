package com.webank.webase.front.keystore;

import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping(value = "{groupId}/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;


    @ApiOperation(value = "get PrivateKey", notes = "get PrivateKey")
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo getPrivateKey() {
        return keyStoreService.createPrivateKey();
    }

    @ApiOperation(value = "import PrivateKey", notes = "import PrivateKey")
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo importPrivateKey(String privateKey) {
        return keyStoreService.getKeyStoreFromPrivateKey(privateKey);
    }
}
