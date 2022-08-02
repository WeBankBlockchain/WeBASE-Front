package com.webank.webase.front.transaction.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;

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
