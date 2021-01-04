package com.webank.webase.front.transaction.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Type;

import java.util.List;

/**
 * abi's function information.
 */
@Data
@Builder
@Accessors(chain = true)
public class ContractFunction {
    String funcName;
    Boolean constant;
    List<String> inputList;
    List<String> outputList;
    List<Type> finalInputs;
    List<TypeReference<?>> finalOutputs;
}
