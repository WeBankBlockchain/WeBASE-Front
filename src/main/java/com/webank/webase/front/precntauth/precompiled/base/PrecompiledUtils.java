package com.webank.webase.front.precntauth.precompiled.base;

import com.webank.webase.front.base.code.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import com.webank.webase.front.base.response.BaseResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.codec.FunctionReturnDecoderInterface;
import org.fisco.bcos.sdk.v3.codec.Utils;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@Slf4j
public class PrecompiledUtils {


  public static String handleTransactionReceipt(TransactionReceipt receipt, boolean isWasm) {
    log.debug("handle tx receipt of precompiled");
    try {
      RetCode sdkRetCode = ReceiptParser.parseTransactionReceipt(
          receipt,
          tr -> {
            FunctionReturnDecoderInterface decoderInterface =
                isWasm
                    ? new org.fisco.bcos.sdk.v3.codec.scale.FunctionReturnDecoder()
                    : new org.fisco.bcos.sdk.v3.codec.abi.FunctionReturnDecoder();
            List<Type> decode =
                decoderInterface.decode(
                    tr.getOutput(),
                    Utils.convert(
                        Collections.singletonList(
                            new TypeReference<Int32>() {})));
            return (BigInteger) decode.get(0).getValue();
          });
      log.info("handleTransactionReceipt sdkRetCode:{}", sdkRetCode);
      if (sdkRetCode.getCode() >= 0) {
        return new BaseResponse(ConstantCode.RET_SUCCESS,
            sdkRetCode.getMessage()).toString();
      } else {
        throw new FrontException(sdkRetCode.getCode(), sdkRetCode.getMessage());
      }
    } catch (ContractException e) {
      log.error("handleTransactionReceipt e:[]", e);
      throw new FrontException(e.getErrorCode(), e.getMessage());
    }
  }

  public static List<String> path2Level(String absolutePath) throws Exception {
    Stack<String> pathStack = new Stack<>();
    for (String s : absolutePath.split("/")) {
      if (s.isEmpty() || s.equals(".")) {
        continue;
      }
      if (s.equals("..")) {
        if (!pathStack.isEmpty()) {
          pathStack.pop();
        }
        continue;
      }
      if (!s.matches("^[0-9a-zA-Z-_]{1,56}$")) {
        throw new Exception("path is invalid: " + absolutePath);
      }
      pathStack.push(s);
    }
    return new ArrayList<>(pathStack);
  }

  public static Tuple2<String, String> getParentPathAndBaseName(String path) throws Exception {
    if (path.equals("/")) return new Tuple2<>("/", "/");
    List<String> path2Level = path2Level(path);
    if (path2Level.isEmpty()) {
      throw new Exception("path is invalid: " + path);
    }
    String baseName = path2Level.get(path2Level.size() - 1);
    String parentPath = '/' + String.join("/", path2Level.subList(0, path2Level.size() - 1));
    return new Tuple2<>(parentPath, baseName);
  }

}
