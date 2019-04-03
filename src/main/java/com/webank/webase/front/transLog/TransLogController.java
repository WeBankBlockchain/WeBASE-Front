package com.webank.webase.front.transLog;

import com.webank.webase.front.base.BaseController;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.contract.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransLogController extends BaseController {

    @Autowired
    TransLogService transLogService;
    @GetMapping
    public List<TransLog> findTransLog(@RequestParam(required = false) String contractName,
                                        @RequestParam(required = false) String version,
                                        @RequestParam(required = false) String contractAddress,
                                        @RequestParam int groupId,  BindingResult result) throws FrontException {
        // log.info("saveAbi start. ReqSendAbi:[{}]", JSON.toJSONString(reqSendAbi));
        checkParamResult(result);
        return  transLogService.findByCriteria(groupId, contractName, version, contractAddress);

    }
}
