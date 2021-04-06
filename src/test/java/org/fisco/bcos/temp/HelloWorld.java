package org.fisco.bcos.temp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class HelloWorld extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610233806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806317308cf414610051578063299f7f9d146100b7575b600080fd5b34801561005d57600080fd5b506100b560048036038101908080359060200190820180359060200190808060200260200160405190810160405280939291908181526020018383602002808284378201915050505050509192919290505050610123565b005b3480156100c357600080fd5b506100cc61013d565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561010f5780820151818401526020810190506100f4565b505050509050019250505060405180910390f35b8060009080519060200190610139929190610195565b5050565b6060600080548060200260200160405190810160405280929190818152602001828054801561018b57602002820191906000526020600020905b815481526020019060010190808311610177575b5050505050905090565b8280548282559060005260206000209081019282156101d1579160200282015b828111156101d05782518255916020019190600101906101b5565b5b5090506101de91906101e2565b5090565b61020491905b808211156102005760008160009055506001016101e8565b5090565b905600a165627a7a72305820cfe57b7fb77799d44c1a29173309ea579b6686c37b228e7e96e2c1d9e210bc420029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50610233806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806317308cf414610051578063299f7f9d146100b7575b600080fd5b34801561005d57600080fd5b506100b560048036038101908080359060200190820180359060200190808060200260200160405190810160405280939291908181526020018383602002808284378201915050505050509192919290505050610123565b005b3480156100c357600080fd5b506100cc61013d565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561010f5780820151818401526020810190506100f4565b505050509050019250505060405180910390f35b8060009080519060200190610139929190610195565b5050565b6060600080548060200260200160405190810160405280929190818152602001828054801561018b57602002820191906000526020600020905b815481526020019060010190808311610177575b5050505050905090565b8280548282559060005260206000209081019282156101d1579160200282015b828111156101d05782518255916020019190600101906101b5565b5b5090506101de91906101e2565b5090565b61020491905b808211156102005760008160009055506001016101e8565b5090565b905600a165627a7a72305820cfe57b7fb77799d44c1a29173309ea579b6686c37b228e7e96e2c1d9e210bc420029"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"name\":\"set\",\"type\":\"function\",\"constant\":false,\"payable\":false,\"anonymous\":false,\"stateMutability\":\"nonpayable\",\"inputs\":[{\"name\":\"_ua\",\"type\":\"uint256[]\",\"indexed\":false,\"components\":null,\"typeAsString\":\"uint256[]\"}],\"outputs\":[],\"methodSignatureAsString\":\"set(uint256[])\"},{\"name\":\"get\",\"type\":\"function\",\"constant\":true,\"payable\":false,\"anonymous\":false,\"stateMutability\":\"view\",\"inputs\":[],\"outputs\":[{\"name\":\"\",\"type\":\"uint256[]\",\"indexed\":false,\"components\":null,\"typeAsString\":\"uint256[]\"}],\"methodSignatureAsString\":\"get()\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_SET = "set";

    public static final String FUNC_GET = "get";

    protected HelloWorld(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt set(List<BigInteger> _ua) {
        final Function function = new Function(
            FUNC_SET,
            Arrays.<Type>asList(_ua.isEmpty()?org.fisco.bcos.sdk.abi.datatypes.DynamicArray.empty("uint256[]"):new org.fisco.bcos.sdk.abi.datatypes.DynamicArray<org.fisco.bcos.sdk.abi.datatypes.generated.Uint256>(
                org.fisco.bcos.sdk.abi.Utils.typeMap(_ua, org.fisco.bcos.sdk.abi.datatypes.generated.Uint256.class))),
            Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void set(List<BigInteger> _ua, TransactionCallback callback) {
        final Function function = new Function(
            FUNC_SET,
            Arrays.<Type>asList(_ua.isEmpty()?org.fisco.bcos.sdk.abi.datatypes.DynamicArray.empty("uint256[]"):new org.fisco.bcos.sdk.abi.datatypes.DynamicArray<org.fisco.bcos.sdk.abi.datatypes.generated.Uint256>(
                org.fisco.bcos.sdk.abi.Utils.typeMap(_ua, org.fisco.bcos.sdk.abi.datatypes.generated.Uint256.class))),
            Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(List<BigInteger> _ua) {
        final Function function = new Function(
            FUNC_SET,
            Arrays.<Type>asList(_ua.isEmpty()?org.fisco.bcos.sdk.abi.datatypes.DynamicArray.empty("uint256[]"):new org.fisco.bcos.sdk.abi.datatypes.DynamicArray<org.fisco.bcos.sdk.abi.datatypes.generated.Uint256>(
                org.fisco.bcos.sdk.abi.Utils.typeMap(_ua, org.fisco.bcos.sdk.abi.datatypes.generated.Uint256.class))),
            Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<List<BigInteger>> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SET,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<List<BigInteger>>(

            convertToNative((List<Uint256>) results.get(0).getValue())
        );
    }

    public List get() throws ContractException {
        final Function function = new Function(FUNC_GET,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        List<Type> result = (List<Type>) executeCallWithSingleValueReturn(function, List.class);
        return convertToNative(result);
    }

    public static HelloWorld load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new HelloWorld(contractAddress, client, credential);
    }

    public static HelloWorld deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(HelloWorld.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }
}
