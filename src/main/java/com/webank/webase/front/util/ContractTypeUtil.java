package com.webank.webase.front.util;

import com.webank.webase.front.base.ConstantCode;
import com.webank.webase.front.base.exception.FrontException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.Bytes;
import org.bcos.web3j.abi.datatypes.DynamicArray;
import org.bcos.web3j.abi.datatypes.DynamicBytes;
import org.bcos.web3j.abi.datatypes.NumericType;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.*;

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
     * encodeString.
     * 
     * @param input input
     * @param type type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Type> T encodeString(String input, Class<T> type)
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
            } else {
                throw new FrontException(201201,
                        String.format("type:%s unsupported encoding", type.getName()));
            }
        } catch (FrontException e) {
            throw e;
        } catch (Exception e) {
            log.error("encodeString failed input:{} type:{}", input, type.getName());
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
            case "ufixed8x248":
                return new TypeReference<DynamicArray<Ufixed8x248>>() {};
            case "fixed8x248":
                return new TypeReference<DynamicArray<Fixed8x248>>() {};
            case "ufixed16x240":
                return new TypeReference<DynamicArray<Ufixed16x240>>() {};
            case "fixed16x240":
                return new TypeReference<DynamicArray<Fixed16x240>>() {};
            case "ufixed24x232":
                return new TypeReference<DynamicArray<Ufixed24x232>>() {};
            case "fixed24x232":
                return new TypeReference<DynamicArray<Fixed24x232>>() {};
            case "ufixed32x224":
                return new TypeReference<DynamicArray<Ufixed32x224>>() {};
            case "fixed32x224":
                return new TypeReference<DynamicArray<Fixed32x224>>() {};
            case "ufixed40x216":
                return new TypeReference<DynamicArray<Ufixed40x216>>() {};
            case "fixed40x216":
                return new TypeReference<DynamicArray<Fixed40x216>>() {};
            case "ufixed48x208":
                return new TypeReference<DynamicArray<Ufixed48x208>>() {};
            case "fixed48x208":
                return new TypeReference<DynamicArray<Fixed48x208>>() {};
            case "ufixed56x200":
                return new TypeReference<DynamicArray<Ufixed56x200>>() {};
            case "fixed56x200":
                return new TypeReference<DynamicArray<Fixed56x200>>() {};
            case "ufixed64x192":
                return new TypeReference<DynamicArray<Ufixed64x192>>() {};
            case "fixed64x192":
                return new TypeReference<DynamicArray<Fixed64x192>>() {};
            case "ufixed72x184":
                return new TypeReference<DynamicArray<Ufixed72x184>>() {};
            case "fixed72x184":
                return new TypeReference<DynamicArray<Fixed72x184>>() {};
            case "ufixed80x176":
                return new TypeReference<DynamicArray<Ufixed80x176>>() {};
            case "fixed80x176":
                return new TypeReference<DynamicArray<Fixed80x176>>() {};
            case "ufixed88x168":
                return new TypeReference<DynamicArray<Ufixed88x168>>() {};
            case "fixed88x168":
                return new TypeReference<DynamicArray<Fixed88x168>>() {};
            case "ufixed96x160":
                return new TypeReference<DynamicArray<Ufixed96x160>>() {};
            case "fixed96x160":
                return new TypeReference<DynamicArray<Fixed96x160>>() {};
            case "ufixed104x152":
                return new TypeReference<DynamicArray<Ufixed104x152>>() {};
            case "fixed104x152":
                return new TypeReference<DynamicArray<Fixed104x152>>() {};
            case "ufixed112x144":
                return new TypeReference<DynamicArray<Ufixed112x144>>() {};
            case "fixed112x144":
                return new TypeReference<DynamicArray<Fixed112x144>>() {};
            case "ufixed120x136":
                return new TypeReference<DynamicArray<Ufixed120x136>>() {};
            case "fixed120x136":
                return new TypeReference<DynamicArray<Fixed120x136>>() {};
            case "ufixed128x128":
                return new TypeReference<DynamicArray<Ufixed128x128>>() {};
            case "fixed128x128":
                return new TypeReference<DynamicArray<Fixed128x128>>() {};
            case "ufixed136x120":
                return new TypeReference<DynamicArray<Ufixed136x120>>() {};
            case "fixed136x120":
                return new TypeReference<DynamicArray<Fixed136x120>>() {};
            case "ufixed144x112":
                return new TypeReference<DynamicArray<Ufixed144x112>>() {};
            case "fixed144x112":
                return new TypeReference<DynamicArray<Fixed144x112>>() {};
            case "ufixed152x104":
                return new TypeReference<DynamicArray<Ufixed152x104>>() {};
            case "fixed152x104":
                return new TypeReference<DynamicArray<Fixed152x104>>() {};
            case "ufixed160x96":
                return new TypeReference<DynamicArray<Ufixed160x96>>() {};
            case "fixed160x96":
                return new TypeReference<DynamicArray<Fixed160x96>>() {};
            case "ufixed168x88":
                return new TypeReference<DynamicArray<Ufixed168x88>>() {};
            case "fixed168x88":
                return new TypeReference<DynamicArray<Fixed168x88>>() {};
            case "ufixed176x80":
                return new TypeReference<DynamicArray<Ufixed176x80>>() {};
            case "fixed176x80":
                return new TypeReference<DynamicArray<Fixed176x80>>() {};
            case "ufixed184x72":
                return new TypeReference<DynamicArray<Ufixed184x72>>() {};
            case "fixed184x72":
                return new TypeReference<DynamicArray<Fixed184x72>>() {};
            case "ufixed192x64":
                return new TypeReference<DynamicArray<Ufixed192x64>>() {};
            case "fixed192x64":
                return new TypeReference<DynamicArray<Fixed192x64>>() {};
            case "ufixed200x56":
                return new TypeReference<DynamicArray<Ufixed200x56>>() {};
            case "fixed200x56":
                return new TypeReference<DynamicArray<Fixed200x56>>() {};
            case "ufixed208x48":
                return new TypeReference<DynamicArray<Ufixed208x48>>() {};
            case "fixed208x48":
                return new TypeReference<DynamicArray<Fixed208x48>>() {};
            case "ufixed216x40":
                return new TypeReference<DynamicArray<Ufixed216x40>>() {};
            case "fixed216x40":
                return new TypeReference<DynamicArray<Fixed216x40>>() {};
            case "ufixed224x32":
                return new TypeReference<DynamicArray<Ufixed224x32>>() {};
            case "fixed224x32":
                return new TypeReference<DynamicArray<Fixed224x32>>() {};
            case "ufixed232x24":
                return new TypeReference<DynamicArray<Ufixed232x24>>() {};
            case "fixed232x24":
                return new TypeReference<DynamicArray<Fixed232x24>>() {};
            case "ufixed240x16":
                return new TypeReference<DynamicArray<Ufixed240x16>>() {};
            case "fixed240x16":
                return new TypeReference<DynamicArray<Fixed240x16>>() {};
            case "ufixed248x8":
                return new TypeReference<DynamicArray<Ufixed248x8>>() {};
            case "fixed248x8":
                return new TypeReference<DynamicArray<Fixed248x8>>() {};
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
     * getType.
     * 
     * @param type type
     * @return
     */
    public static Class<? extends Type> getType(String type) throws FrontException {
        switch (type) {
            case "address":
                return Address.class;
            case "bool":
                return Bool.class;
            case "string":
                return Utf8String.class;
            case "bytes":
                return DynamicBytes.class;
            case "uint8":
                return Uint8.class;
            case "int8":
                return Int8.class;
            case "uint16":
                return Uint16.class;
            case "int16":
                return Int16.class;
            case "uint24":
                return Uint24.class;
            case "int24":
                return Int24.class;
            case "uint32":
                return Uint32.class;
            case "int32":
                return Int32.class;
            case "uint40":
                return Uint40.class;
            case "int40":
                return Int40.class;
            case "uint48":
                return Uint48.class;
            case "int48":
                return Int48.class;
            case "uint56":
                return Uint56.class;
            case "int56":
                return Int56.class;
            case "uint64":
                return Uint64.class;
            case "int64":
                return Int64.class;
            case "uint72":
                return Uint72.class;
            case "int72":
                return Int72.class;
            case "uint80":
                return Uint80.class;
            case "int80":
                return Int80.class;
            case "uint88":
                return Uint88.class;
            case "int88":
                return Int88.class;
            case "uint96":
                return Uint96.class;
            case "int96":
                return Int96.class;
            case "uint104":
                return Uint104.class;
            case "int104":
                return Int104.class;
            case "uint112":
                return Uint112.class;
            case "int112":
                return Int112.class;
            case "uint120":
                return Uint120.class;
            case "int120":
                return Int120.class;
            case "uint128":
                return Uint128.class;
            case "int128":
                return Int128.class;
            case "uint136":
                return Uint136.class;
            case "int136":
                return Int136.class;
            case "uint144":
                return Uint144.class;
            case "int144":
                return Int144.class;
            case "uint152":
                return Uint152.class;
            case "int152":
                return Int152.class;
            case "uint160":
                return Uint160.class;
            case "int160":
                return Int160.class;
            case "uint168":
                return Uint168.class;
            case "int168":
                return Int168.class;
            case "uint176":
                return Uint176.class;
            case "int176":
                return Int176.class;
            case "uint184":
                return Uint184.class;
            case "int184":
                return Int184.class;
            case "uint192":
                return Uint192.class;
            case "int192":
                return Int192.class;
            case "uint200":
                return Uint200.class;
            case "int200":
                return Int200.class;
            case "uint208":
                return Uint208.class;
            case "int208":
                return Int208.class;
            case "uint216":
                return Uint216.class;
            case "int216":
                return Int216.class;
            case "uint224":
                return Uint224.class;
            case "int224":
                return Int224.class;
            case "uint232":
                return Uint232.class;
            case "int232":
                return Int232.class;
            case "uint240":
                return Uint240.class;
            case "int240":
                return Int240.class;
            case "uint248":
                return Uint248.class;
            case "int248":
                return Int248.class;
            case "uint256":
                return Uint256.class;
            case "int256":
                return Int256.class;
            case "ufixed8x248":
                return Ufixed8x248.class;
            case "fixed8x248":
                return Fixed8x248.class;
            case "ufixed16x240":
                return Ufixed16x240.class;
            case "fixed16x240":
                return Fixed16x240.class;
            case "ufixed24x232":
                return Ufixed24x232.class;
            case "fixed24x232":
                return Fixed24x232.class;
            case "ufixed32x224":
                return Ufixed32x224.class;
            case "fixed32x224":
                return Fixed32x224.class;
            case "ufixed40x216":
                return Ufixed40x216.class;
            case "fixed40x216":
                return Fixed40x216.class;
            case "ufixed48x208":
                return Ufixed48x208.class;
            case "fixed48x208":
                return Fixed48x208.class;
            case "ufixed56x200":
                return Ufixed56x200.class;
            case "fixed56x200":
                return Fixed56x200.class;
            case "ufixed64x192":
                return Ufixed64x192.class;
            case "fixed64x192":
                return Fixed64x192.class;
            case "ufixed72x184":
                return Ufixed72x184.class;
            case "fixed72x184":
                return Fixed72x184.class;
            case "ufixed80x176":
                return Ufixed80x176.class;
            case "fixed80x176":
                return Fixed80x176.class;
            case "ufixed88x168":
                return Ufixed88x168.class;
            case "fixed88x168":
                return Fixed88x168.class;
            case "ufixed96x160":
                return Ufixed96x160.class;
            case "fixed96x160":
                return Fixed96x160.class;
            case "ufixed104x152":
                return Ufixed104x152.class;
            case "fixed104x152":
                return Fixed104x152.class;
            case "ufixed112x144":
                return Ufixed112x144.class;
            case "fixed112x144":
                return Fixed112x144.class;
            case "ufixed120x136":
                return Ufixed120x136.class;
            case "fixed120x136":
                return Fixed120x136.class;
            case "ufixed128x128":
                return Ufixed128x128.class;
            case "fixed128x128":
                return Fixed128x128.class;
            case "ufixed136x120":
                return Ufixed136x120.class;
            case "fixed136x120":
                return Fixed136x120.class;
            case "ufixed144x112":
                return Ufixed144x112.class;
            case "fixed144x112":
                return Fixed144x112.class;
            case "ufixed152x104":
                return Ufixed152x104.class;
            case "fixed152x104":
                return Fixed152x104.class;
            case "ufixed160x96":
                return Ufixed160x96.class;
            case "fixed160x96":
                return Fixed160x96.class;
            case "ufixed168x88":
                return Ufixed168x88.class;
            case "fixed168x88":
                return Fixed168x88.class;
            case "ufixed176x80":
                return Ufixed176x80.class;
            case "fixed176x80":
                return Fixed176x80.class;
            case "ufixed184x72":
                return Ufixed184x72.class;
            case "fixed184x72":
                return Fixed184x72.class;
            case "ufixed192x64":
                return Ufixed192x64.class;
            case "fixed192x64":
                return Fixed192x64.class;
            case "ufixed200x56":
                return Ufixed200x56.class;
            case "fixed200x56":
                return Fixed200x56.class;
            case "ufixed208x48":
                return Ufixed208x48.class;
            case "fixed208x48":
                return Fixed208x48.class;
            case "ufixed216x40":
                return Ufixed216x40.class;
            case "fixed216x40":
                return Fixed216x40.class;
            case "ufixed224x32":
                return Ufixed224x32.class;
            case "fixed224x32":
                return Fixed224x32.class;
            case "ufixed232x24":
                return Ufixed232x24.class;
            case "fixed232x24":
                return Fixed232x24.class;
            case "ufixed240x16":
                return Ufixed240x16.class;
            case "fixed240x16":
                return Fixed240x16.class;
            case "ufixed248x8":
                return Ufixed248x8.class;
            case "fixed248x8":
                return Fixed248x8.class;
            case "bytes1":
                return Bytes1.class;
            case "bytes2":
                return Bytes2.class;
            case "bytes3":
                return Bytes3.class;
            case "bytes4":
                return Bytes4.class;
            case "bytes5":
                return Bytes5.class;
            case "bytes6":
                return Bytes6.class;
            case "bytes7":
                return Bytes7.class;
            case "bytes8":
                return Bytes8.class;
            case "bytes9":
                return Bytes9.class;
            case "bytes10":
                return Bytes10.class;
            case "bytes11":
                return Bytes11.class;
            case "bytes12":
                return Bytes12.class;
            case "bytes13":
                return Bytes13.class;
            case "bytes14":
                return Bytes14.class;
            case "bytes15":
                return Bytes15.class;
            case "bytes16":
                return Bytes16.class;
            case "bytes17":
                return Bytes17.class;
            case "bytes18":
                return Bytes18.class;
            case "bytes19":
                return Bytes19.class;
            case "bytes20":
                return Bytes20.class;
            case "bytes21":
                return Bytes21.class;
            case "bytes22":
                return Bytes22.class;
            case "bytes23":
                return Bytes23.class;
            case "bytes24":
                return Bytes24.class;
            case "bytes25":
                return Bytes25.class;
            case "bytes26":
                return Bytes26.class;
            case "bytes27":
                return Bytes27.class;
            case "bytes28":
                return Bytes28.class;
            case "bytes29":
                return Bytes29.class;
            case "bytes30":
                return Bytes30.class;
            case "bytes31":
                return Bytes31.class;
            case "bytes32":
                return Bytes32.class;
            default:
                throw new FrontException(201201,
                        String.format("type:%s unsupported encoding", type));
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
