package com.webank.webase.front.keystore;

import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.BaseResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping(value = "/privateKey")
public class KeyStoreController extends BaseController {

    @Autowired
    private KeyStoreService keyStoreService;

    @ApiOperation(value = "get PrivateKey", notes = "get PrivateKey")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean"),
        @ApiImplicitParam(name = "userName", value = "new userName", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    public KeyStoreInfo getPrivateKey(boolean useAes, String userName) {
      //  return keyStoreService.createPrivateKey(useAes);
        return keyStoreService.newUser(useAes,userName);
    }

  /*  @ApiOperation(value = "new user", notes = "new user")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "useAes", value = "Is encrypting the private key", dataType = "Boolean"),
        @ApiImplicitParam(name = "userName", value = "new userName", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    @GetMapping("/newUser/{userName:[a-zA-Z0-9_]{3,32}}")
    public KeyStoreInfo getUserInfo(@PathVariable("userName") String userName) {
        return keyStoreService.newUser(userName);
    }*/

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
