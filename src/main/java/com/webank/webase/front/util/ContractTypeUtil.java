package com.webank.webase.front.util;

import com.squareup.javapoet.TypeName;
import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bool;
import org.fisco.bcos.web3j.abi.datatypes.Bytes;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.abi.datatypes.DynamicBytes;
import org.fisco.bcos.web3j.abi.datatypes.NumericType;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.*;

/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ContractTypeUtil.
 *
 */
@Slf4j
public class ContractTypeUtil {

    /**
     * generateClassFromInput.
     * 
     * @param input input
     * @param type type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Type> T generateClassFromInput(String input, Class<T> type)
            throws FrontException {
        try {
            if (Address.class.isAssignableFrom(type)) {
                return (T) new Address(input);
            } else if (NumericType.class.isAssignableFrom(type)) {
                return (T) encodeNumeric(input, (Class<NumericType>) type);
            } else if (Bool.class.isAssignableFrom(type)) {
                return (T) new Bool(Boolean.valueOf(input));
            } else if (Utf8String.class.isAssignableFrom(type)) {
                return (T) new Utf8String(input);
            } else if (Bytes.class.isAssignableFrom(type)) {
                return (T) encodeBytes(input, (Class<Bytes>) type);
            } else if (DynamicBytes.class.isAssignableFrom(type)) {
                return (T) new DynamicBytes(input.getBytes());
                //todo static
            } else {
                throw new FrontException(201201,
                        String.format("type:%s unsupported encoding", type.getName()));
            }
        } catch (FrontException e) {
            throw e;
        } catch (Exception e) {
            log.error("generateClassFromInput failed input:{} type:{}", input, type.getName());
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
    }

    /**
     * decodeResult.
     * 
     * @param result result
     * @param type type
     * @return
     */
    public static <T> Object decodeResult(Type result, Class<T> type) throws FrontException {
        try {
            if (Address.class.isAssignableFrom(type)) {
                return result.toString();
            } else if (NumericType.class.isAssignableFrom(type)) {
                return result.getValue();
            } else if (Bool.class.isAssignableFrom(type)) {
                return result.getValue();
            } else if (Utf8String.class.isAssignableFrom(type)) {
                return result.getValue().toString();
            } else if (Bytes.class.isAssignableFrom(type)) {
                return decodeBytes((byte[]) result.getValue());
            } else if (DynamicBytes.class.isAssignableFrom(type)) {
                return decodeBytes((byte[]) result.getValue());
            } else {
                throw new FrontException(201202,
                        String.format("type:%s unsupported decoding", type.getName()));
            }
        } catch (FrontException e) {
            throw e;
        } catch (Exception e) {
            log.error("decodeResult failed result:{} type:{}", result, type.getName());
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
    }

    static <T extends NumericType> T encodeNumeric(String input, Class<T> type)
            throws FrontException {
        try {
            BigInteger numericValue = new BigInteger(input);
            return type.getConstructor(BigInteger.class).newInstance(numericValue);
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("encodeNumeric failed.");
            throw new FrontException(201203,
                    String.format("unable to create instance of type:%s", type.getName()));
        }
    }

    static <T extends Bytes> T encodeBytes(String input, Class<T> type) throws FrontException {
        try {
            String simpleName = type.getSimpleName();
            String[] splitName = simpleName.split(Bytes.class.getSimpleName());
            int length = Integer.parseInt(splitName[1]);

            byte[] byteValue = null;
            if (input.length() > length) {
                byteValue = input.substring(0, length).getBytes();
            } else {
                byteValue = input.getBytes();
            }
            byte[] byteValueLength = new byte[length];
            System.arraycopy(byteValue, 0, byteValueLength, 0, byteValue.length);

            return type.getConstructor(byte[].class).newInstance(byteValueLength);
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("encodeBytes failed.");
            throw new FrontException(201203,
                    String.format("unable to create instance of type:%s", type.getName()));
        }
    }

    /**
     * decodeBytes.
     * 
     * @param data data
     * @return
     */
    public static String decodeBytes(byte[] data) throws FrontException {
        try {
            int offset = searchByte(data, (byte) 0);
            if (offset != -1) {
                return new String(data, 0, offset, "UTF-8");
            } else {
                return new String(data, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            log.error("decodeBytes failed.");
            throw new FrontException(201202,
                    String.format("type:byte%s unsupported decoding", data.length));
        }
    }

    /**
     * searchByte.
     * 
     * @param data data
     * @param value value
     * @return
     */
    public static int searchByte(byte[] data, byte value) {
        int size = data.length;
        for (int i = 0; i < size; ++i) {
            if (data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * getArrayType.
     *
     * @param type type
     * @return
     */
    public static TypeReference<?> getArrayType(String type) throws FrontException {
        switch (type) {
            case "address":
                return new TypeReference<DynamicArray<Address>>() {};
            case "bool":
                return new TypeReference<DynamicArray<Bool>>() {};
            case "string":
                return new TypeReference<DynamicArray<Utf8String>>() {};
            case "bytes":
                return new TypeReference<DynamicArray<DynamicBytes>>() {};
            case "uint8":
                return new TypeReference<DynamicArray<Uint8>>() {};
            case "int8":
                return new TypeReference<DynamicArray<Int8>>() {};
            case "uint16":
                return new TypeReference<DynamicArray<Uint16>>() {};
            case "int16":
                return new TypeReference<DynamicArray<Int16>>() {};
            case "uint24":
                return new TypeReference<DynamicArray<Uint24>>() {};
            case "int24":
                return new TypeReference<DynamicArray<Int24>>() {};
            case "uint32":
                return new TypeReference<DynamicArray<Uint32>>() {};
            case "int32":
                return new TypeReference<DynamicArray<Int32>>() {};
            case "uint40":
                return new TypeReference<DynamicArray<Uint40>>() {};
            case "int40":
                return new TypeReference<DynamicArray<Int40>>() {};
            case "uint48":
                return new TypeReference<DynamicArray<Uint48>>() {};
            case "int48":
                return new TypeReference<DynamicArray<Int48>>() {};
            case "uint56":
                return new TypeReference<DynamicArray<Uint56>>() {};
            case "int56":
                return new TypeReference<DynamicArray<Int56>>() {};
            case "uint64":
                return new TypeReference<DynamicArray<Uint64>>() {};
            case "int64":
                return new TypeReference<DynamicArray<Int64>>() {};
            case "uint72":
                return new TypeReference<DynamicArray<Uint72>>() {};
            case "int72":
                return new TypeReference<DynamicArray<Int72>>() {};
            case "uint80":
                return new TypeReference<DynamicArray<Uint80>>() {};
            case "int80":
                return new TypeReference<DynamicArray<Int80>>() {};
            case "uint88":
                return new TypeReference<DynamicArray<Uint88>>() {};
            case "int88":
                return new TypeReference<DynamicArray<Int88>>() {};
            case "uint96":
                return new TypeReference<DynamicArray<Uint96>>() {};
            case "int96":
                return new TypeReference<DynamicArray<Int96>>() {};
            case "uint104":
                return new TypeReference<DynamicArray<Uint104>>() {};
            case "int104":
                return new TypeReference<DynamicArray<Int104>>() {};
            case "uint112":
                return new TypeReference<DynamicArray<Uint112>>() {};
            case "int112":
                return new TypeReference<DynamicArray<Int112>>() {};
            case "uint120":
                return new TypeReference<DynamicArray<Uint120>>() {};
            case "int120":
                return new TypeReference<DynamicArray<Int120>>() {};
            case "uint128":
                return new TypeReference<DynamicArray<Uint128>>() {};
            case "int128":
                return new TypeReference<DynamicArray<Int128>>() {};
            case "uint136":
                return new TypeReference<DynamicArray<Uint136>>() {};
            case "int136":
                return new TypeReference<DynamicArray<Int136>>() {};
            case "uint144":
                return new TypeReference<DynamicArray<Uint144>>() {};
            case "int144":
                return new TypeReference<DynamicArray<Int144>>() {};
            case "uint152":
                return new TypeReference<DynamicArray<Uint152>>() {};
            case "uint160":
                return new TypeReference<DynamicArray<Uint160>>() {};
            case "int160":
                return new TypeReference<DynamicArray<Int160>>() {};
            case "uint168":
                return new TypeReference<DynamicArray<Uint168>>() {};
            case "int168":
                return new TypeReference<DynamicArray<Int168>>() {};
            case "uint176":
                return new TypeReference<DynamicArray<Uint176>>() {};
            case "int176":
                return new TypeReference<DynamicArray<Int176>>() {};
            case "uint184":
                return new TypeReference<DynamicArray<Uint184>>() {};
            case "int184":
                return new TypeReference<DynamicArray<Int184>>() {};
            case "uint192":
                return new TypeReference<DynamicArray<Uint192>>() {};
            case "int192":
                return new TypeReference<DynamicArray<Int192>>() {};
            case "uint200":
                return new TypeReference<DynamicArray<Uint200>>() {};
            case "int200":
                return new TypeReference<DynamicArray<Int200>>() {};
            case "uint208":
                return new TypeReference<DynamicArray<Uint208>>() {};
            case "int208":
                return new TypeReference<DynamicArray<Int208>>() {};
            case "uint216":
                return new TypeReference<DynamicArray<Uint216>>() {};
            case "int216":
                return new TypeReference<DynamicArray<Int216>>() {};
            case "uint224":
                return new TypeReference<DynamicArray<Uint224>>() {};
            case "int224":
                return new TypeReference<DynamicArray<Int224>>() {};
            case "uint232":
                return new TypeReference<DynamicArray<Uint232>>() {};
            case "int232":
                return new TypeReference<DynamicArray<Int232>>() {};
            case "uint240":
                return new TypeReference<DynamicArray<Uint240>>() {};
            case "int240":
                return new TypeReference<DynamicArray<Int240>>() {};
            case "uint248":
                return new TypeReference<DynamicArray<Uint248>>() {};
            case "int248":
                return new TypeReference<DynamicArray<Int248>>() {};
            case "uint256":
                return new TypeReference<DynamicArray<Uint256>>() {};
            case "int256":
                return new TypeReference<DynamicArray<Int256>>() {};
            case "bytes1":
                return new TypeReference<DynamicArray<Bytes1>>() {};
            case "bytes2":
                return new TypeReference<DynamicArray<Bytes2>>() {};
            case "bytes3":
                return new TypeReference<DynamicArray<Bytes3>>() {};
            case "bytes4":
                return new TypeReference<DynamicArray<Bytes4>>() {};
            case "bytes5":
                return new TypeReference<DynamicArray<Bytes5>>() {};
            case "bytes6":
                return new TypeReference<DynamicArray<Bytes6>>() {};
            case "bytes7":
                return new TypeReference<DynamicArray<Bytes7>>() {};
            case "bytes8":
                return new TypeReference<DynamicArray<Bytes8>>() {};
            case "bytes9":
                return new TypeReference<DynamicArray<Bytes9>>() {};
            case "bytes10":
                return new TypeReference<DynamicArray<Bytes10>>() {};
            case "bytes11":
                return new TypeReference<DynamicArray<Bytes11>>() {};
            case "bytes12":
                return new TypeReference<DynamicArray<Bytes12>>() {};
            case "bytes13":
                return new TypeReference<DynamicArray<Bytes13>>() {};
            case "bytes14":
                return new TypeReference<DynamicArray<Bytes14>>() {};
            case "bytes15":
                return new TypeReference<DynamicArray<Bytes15>>() {};
            case "bytes16":
                return new TypeReference<DynamicArray<Bytes16>>() {};
            case "bytes17":
                return new TypeReference<DynamicArray<Bytes17>>() {};
            case "bytes18":
                return new TypeReference<DynamicArray<Bytes18>>() {};
            case "bytes19":
                return new TypeReference<DynamicArray<Bytes19>>() {};
            case "bytes20":
                return new TypeReference<DynamicArray<Bytes20>>() {};
            case "bytes21":
                return new TypeReference<DynamicArray<Bytes21>>() {};
            case "bytes22":
                return new TypeReference<DynamicArray<Bytes22>>() {};
            case "bytes23":
                return new TypeReference<DynamicArray<Bytes23>>() {};
            case "bytes24":
                return new TypeReference<DynamicArray<Bytes24>>() {};
            case "bytes25":
                return new TypeReference<DynamicArray<Bytes25>>() {};
            case "bytes26":
                return new TypeReference<DynamicArray<Bytes26>>() {};
            case "bytes27":
                return new TypeReference<DynamicArray<Bytes27>>() {};
            case "bytes28":
                return new TypeReference<DynamicArray<Bytes28>>() {};
            case "bytes29":
                return new TypeReference<DynamicArray<Bytes29>>() {};
            case "bytes30":
                return new TypeReference<DynamicArray<Bytes30>>() {};
            case "bytes31":
                return new TypeReference<DynamicArray<Bytes31>>() {};
            case "bytes32":
                return new TypeReference<DynamicArray<Bytes32>>() {};
            default:
                throw new FrontException(201201,
                        String.format("type:%s array unsupported encoding", type));
        }

    }


    /**
     * parseByType.
     * 
     * @param type type
     * @param value value
     * @return
     */
    public static Object parseByType(String type, String value) throws FrontException {
        try {
            switch (type) {
                case "address":
                    return value;
                case "bool":
                    return Boolean.valueOf(value);
                case "string":
                    return value;
                case "uint8":
                case "int8":
                case "uint16":
                case "int16":
                case "uint24":
                case "int24":
                case "uint32":
                case "int32":
                case "uint40":
                case "int40":
                case "uint48":
                case "int48":
                case "uint56":
                case "int56":
                case "uint64":
                case "int64":
                case "uint72":
                case "int72":
                case "uint80":
                case "int80":
                case "uint88":
                case "int88":
                case "uint96":
                case "int96":
                case "uint104":
                case "int104":
                case "uint112":
                case "int112":
                case "uint120":
                case "int120":
                case "uint128":
                case "int128":
                case "uint136":
                case "int136":
                case "uint144":
                case "int144":
                case "uint152":
                case "int152":
                case "uint160":
                case "int160":
                case "uint168":
                case "int168":
                case "uint176":
                case "int176":
                case "uint184":
                case "int184":
                case "uint192":
                case "int192":
                case "uint200":
                case "int200":
                case "uint208":
                case "int208":
                case "uint216":
                case "int216":
                case "uint224":
                case "int224":
                case "uint232":
                case "int232":
                case "uint240":
                case "int240":
                case "uint248":
                case "int248":
                case "uint256":
                case "int256":
                    return new BigInteger(value);
                case "bytes1":
                case "bytes2":
                case "bytes3":
                case "bytes4":
                case "bytes5":
                case "bytes6":
                case "bytes7":
                case "bytes8":
                case "bytes9":
                case "bytes10":
                case "bytes11":
                case "bytes12":
                case "bytes13":
                case "bytes14":
                case "bytes15":
                case "bytes16":
                case "bytes17":
                case "bytes18":
                case "bytes19":
                case "bytes20":
                case "bytes21":
                case "bytes22":
                case "bytes23":
                case "bytes24":
                case "bytes25":
                case "bytes26":
                case "bytes27":
                case "bytes28":
                case "bytes29":
                case "bytes30":
                case "bytes31":
                case "bytes32":
                case "bytes":
                    return value;
                default:
                    throw new FrontException(201201,
                            String.format("type:%s unsupported encoding", type));
            }
        } catch (Exception e) {
            log.error("parseByType failed type:{} value:{}", type, value);
            throw new FrontException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
    }
}
