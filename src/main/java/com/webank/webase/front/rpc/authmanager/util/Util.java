//package com.webank.webase.front.rpc.authmanager.util;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.extern.slf4j.Slf4j;
//import org.fisco.bcos.sdk.codec.ABICodecException;
//import org.fisco.bcos.sdk.codec.FunctionEncoderInterface;
//import org.fisco.bcos.sdk.codec.datatypes.Address;
//import org.fisco.bcos.sdk.codec.datatypes.Bool;
//import org.fisco.bcos.sdk.codec.datatypes.DynamicArray;
//import org.fisco.bcos.sdk.codec.datatypes.DynamicBytes;
//import org.fisco.bcos.sdk.codec.datatypes.DynamicStruct;
//import org.fisco.bcos.sdk.codec.datatypes.StaticArray;
//import org.fisco.bcos.sdk.codec.datatypes.StaticStruct;
//import org.fisco.bcos.sdk.codec.datatypes.Type;
//import org.fisco.bcos.sdk.codec.datatypes.Utf8String;
//import org.fisco.bcos.sdk.codec.wrapper.ABICodecJsonWrapper;
//import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
//import org.fisco.bcos.sdk.codec.wrapper.ABIDefinitionFactory;
//import org.fisco.bcos.sdk.codec.wrapper.ContractABIDefinition;
//import org.fisco.bcos.sdk.crypto.CryptoSuite;
//import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
//
//@Slf4j
//public class Util {
//
//  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
//
//  private final ABIDefinitionFactory abiDefinitionFactory;
//
//  public Util(CryptoSuite cryptoSuite) {
//    this.abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
//  }
//
//  public byte[] encodeMethodByABIAndMethod(String abi, String methodName, List<String> params)
//      throws ABICodecException {
//    ContractABIDefinition contractABIDefinition = this.abiDefinitionFactory.loadABI(abi);
//    List<ABIDefinition> methods = contractABIDefinition.getFunctions().get(methodName);
//    if (methods == null) {
//      log.debug(
//          "Invalid methodName: {}, all the functions are: {}",
//          methodName,
//          contractABIDefinition.getFunctions());
//      throw new ABICodecException(
//          "Invalid method "
//              + methodName
//              + " , supported functions are: "
//              + contractABIDefinition.getFunctions().keySet());
//    }
//
//    for (ABIDefinition abiDefinition : methods) {
//      if (abiDefinition.getInputs().size() == params.size()) {
//        List<ABIDefinition.NamedType> inputs = abiDefinition.getInputs();
//        List<Type> inputTypes = new ArrayList<>();
//        try {
//          for (int i = 0; i < inputs.size(); ++i) {
//            inputTypes.add(buildType(inputs.get(i), params.get(i)));
//          }
//          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//          String signature =
//              FunctionEncoderInterface.buildMethodSignature(
//                  abiDefinition.getName(), inputTypes);
//
//          return outputStream.toByteArray();
//        } catch (IOException e) {
//          log.error(" exception in encodeMethodFromString : {}", e.getMessage());
//        }
//      }
//    }
//
//    String errorMsg =
//        " cannot encode in encodeMethodFromString with appropriate interface ABI, make sure params match";
//    log.error(errorMsg);
//    throw new ABICodecException(errorMsg);
//  }
//
//  private Type buildType(ABIDefinition.NamedType namedType, String param)
//      throws ABICodecException, IOException {
//    String typeStr = namedType.getType();
//    ABIDefinition.Type paramType = new ABIDefinition.Type(typeStr);
//    Type type = null;
//    if (paramType.isList()) {
//      List elements = new ArrayList();
//      JsonNode jsonNode = this.objectMapper.readTree(param);
//      assert jsonNode.isArray();
//
//      ABIDefinition.NamedType subType = new ABIDefinition.NamedType();
//      subType.setType(paramType.reduceDimensionAndGetType().getType());
//      subType.setComponents(namedType.getComponents());
//
//      for (JsonNode subNode : jsonNode) {
//        String subNodeStr =
//            subNode.isTextual()
//                ? subNode.asText()
//                : this.objectMapper.writeValueAsString(subNode);
//        Type element = buildType(subType, subNodeStr);
//        elements.add(element);
//      }
//      type = paramType.isFixedList() ? new StaticArray(elements) : new DynamicArray(elements);
//      return type;
//    } else if (typeStr.equals("tuple")) {
//      List<Type> components = new ArrayList<>();
//      JsonNode jsonNode = this.objectMapper.readTree(param);
//      assert jsonNode.isObject();
//      for (ABIDefinition.NamedType component : namedType.getComponents()) {
//        JsonNode subNode = jsonNode.get(component.getName());
//        String subNodeStr =
//            subNode.isTextual()
//                ? subNode.asText()
//                : this.objectMapper.writeValueAsString(subNode);
//        components.add(buildType(component, subNodeStr));
//      }
//      type =
//          namedType.isDynamic()
//              ? new DynamicStruct(components)
//              : new StaticStruct(components);
//      return type;
//    } else {
//      if (typeStr.startsWith("uint")) {
//        int bitSize = 256;
//        if (!typeStr.equals("uint")) {
//          String bitSizeStr = typeStr.substring("uint".length());
//          try {
//            bitSize = Integer.parseInt(bitSizeStr);
//          } catch (NumberFormatException e) {
//            String errorMsg =
//                " unrecognized type: " + typeStr + ", error:" + e.getCause();
//            log.error(errorMsg);
//            throw new ABICodecException(errorMsg);
//          }
//        }
//
//        try {
//          Class<?> uintClass =
//              Class.forName(
//                  "org.fisco.bcos.sdk.codec.datatypes.generated.Uint" + bitSize);
//          type =
//              (Type)
//                  uintClass
//                      .getDeclaredConstructor(BigInteger.class)
//                      .newInstance(new BigInteger(param));
//        } catch (ClassNotFoundException
//            | NoSuchMethodException
//            | InstantiationException
//            | IllegalAccessException
//            | InvocationTargetException e) {
//          String errorMsg =
//              "buildType error, type: " + typeStr + ", error: " + e.getCause();
//          log.error(errorMsg);
//          throw new ABICodecException(errorMsg);
//        }
//
//        return type;
//      }
//
//      if (typeStr.startsWith("int")) {
//        int bitSize = 256;
//        if (!typeStr.equals("int")) {
//          String bitSizeStr = typeStr.substring("int".length());
//          try {
//            bitSize = Integer.parseInt(bitSizeStr);
//          } catch (NumberFormatException e) {
//            String errorMsg = "unrecognized int type: " + typeStr;
//            log.error(errorMsg);
//            throw new ABICodecException(errorMsg);
//          }
//        }
//
//        try {
//          Class<?> uintClass =
//              Class.forName(
//                  "org.fisco.bcos.sdk.codec.datatypes.generated.Int" + bitSize);
//          type =
//              (Type)
//                  uintClass
//                      .getDeclaredConstructor(BigInteger.class)
//                      .newInstance(new BigInteger(param));
//        } catch (ClassNotFoundException
//            | NoSuchMethodException
//            | InstantiationException
//            | IllegalAccessException
//            | InvocationTargetException e) {
//          String errorMsg = "unrecognized type: " + typeStr + ", error:" + e.getCause();
//          log.error(errorMsg);
//          throw new ABICodecException(errorMsg);
//        }
//
//        return type;
//      }
//
//      if (typeStr.equals("bool")) {
//        type = new Bool(Boolean.parseBoolean(param));
//        return type;
//      }
//
//      if (typeStr.equals("string")) {
//        type = new Utf8String(param);
//        return type;
//      }
//
//      if (typeStr.equals("bytes")) {
//        byte[] bytes = ABICodecJsonWrapper.tryDecodeInputData(param);
//        if (bytes == null) {
//          bytes = param.getBytes();
//        }
//        type = new DynamicBytes(bytes);
//        return type;
//      }
//
//      if (typeStr.equals("address")) {
//        type = new Address(param);
//        return type;
//      }
//
//      // static bytesN
//      if (typeStr.startsWith("bytes")) {
//        String lengthStr = typeStr.substring("bytes".length());
//        int length;
//        try {
//          length = Integer.parseInt(lengthStr);
//        } catch (NumberFormatException e) {
//          String errorMsg = "unrecognized static byte array type: " + typeStr;
//          log.error(errorMsg);
//          throw new ABICodecException(errorMsg);
//        }
//
//        if (length > 32) {
//          String errorMsg = "the length of static byte array exceeds 32: " + typeStr;
//          log.error(errorMsg);
//          throw new ABICodecException(errorMsg);
//        }
//        byte[] bytesN = ABICodecJsonWrapper.tryDecodeInputData(param);
//        if (bytesN == null) {
//          bytesN = param.getBytes();
//        }
//        if (bytesN.length != length) {
//          String errorMsg =
//              String.format(
//                  "expected byte array at length %d but length of provided in data is %d",
//                  length, bytesN.length);
//          log.error(errorMsg);
//          throw new ABICodecException(errorMsg);
//        }
//
//        try {
//          Class<?> bytesClass =
//              Class.forName(
//                  "org.fisco.bcos.sdk.codec.datatypes.generated.Bytes" + length);
//          type =
//              (Type)
//                  bytesClass
//                      .getDeclaredConstructor(byte[].class)
//                      .newInstance(bytesN);
//        } catch (ClassNotFoundException
//            | NoSuchMethodException
//            | InstantiationException
//            | IllegalAccessException
//            | InvocationTargetException e) {
//          e.printStackTrace();
//        }
//        return type;
//      }
//    }
//    String errorMsg = "unrecognized type: " + typeStr;
//    log.error(errorMsg);
//    throw new ABICodecException(errorMsg);
//  }
//
//
//}
