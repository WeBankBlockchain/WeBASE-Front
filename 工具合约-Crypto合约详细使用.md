# 工具合约-Crypto合约使用

## Crypto合约详细

Crypto的智能合约，它包含4个函数：

1. `sm3(bytes memory data)`：使用Sm3 hash算法处理数据并返回结果。
2. `keccak256Hash(bytes memory data)`：使用Keccak256 hash算法处理数据并返回结果。
3. `sm2Verify(bytes32 message, bytes memory publicKey, bytes32 r, bytes32 s)`：使用SM2签名算法验证信息。
4. `curve25519VRFVerify(string memory input, string memory vrfPublicKey, string memory vrfProof)` ：使用Curve25519 VRF签名算法验证信息，并返回一个布尔值和签名后的随机数。

这个合约提供了不同类型的加密算法支持，在区块链上进行加密、解密和验证操作，并且只有合约创建者才有权提交和执行这些操作。

```solidity
/**
 * @title Crypto
 * @dev 智能合约提供了Sm3, Keccak256, SM2 signature verification和Curve25519 VRF签名算法支持，只能在FISCO-BCOS v2.8.0+上使用。
 */
contract Crypto 
{
    /**
     * @dev 使用Sm3 hash算法处理数据并返回结果。
     * @param data 要进行哈希的数据
     * @return bytes32 哈希结果
     */
    function sm3(bytes memory data) public view returns(bytes32){}
    
    /**
     * @dev 使用Keccak256 hash算法处理数据并返回结果。
     * @param data 要进行哈希的数据
     * @return bytes32 哈希结果
     */
    function keccak256Hash(bytes memory data) public view returns(bytes32){}
    
    /**
     * @dev 使用SM2签名算法验证信息是否正确。
     * @param message 待验信息的哈希值
     * @param publicKey 签名密钥的公钥
     * @param r 签名中的r的值
     * @param s 签名中的s的值
     * @return bool 验证结果true为成功，false为失败
     * @return address 签名验证通过后返回签名地址
     */
    function sm2Verify(bytes32  message, bytes memory publicKey, bytes32 r, bytes32 s) public view returns(bool, address){}
    
    /**
     * @dev 使用Curve25519 VRF签名算法验证输入字符串，并返回一个布尔值和签名后的随机数。 
     * @param input 将要被签名的字符串形式数据
     * @param vrfPublicKey 用于签名的公钥
     * @param vrfProof 签名后的证明内容
     * @return bool 验证结果 true为成功，false为失败
     * @return uint256 返回签名后的随机数
     */
    function curve25519VRFVerify(string memory input, string memory vrfPublicKey, string memory vrfProof) public view returns(bool,uint256){}
}
```

### sm3函数使用

将给定的字符串和数字哈希为一个byte数组，然后计算它的SHA3/Keccak-256哈希。

```solidity
function testSm3(string memory message, uint num) public view returns (bytes32) {
    bytes memory data = abi.encodePacked(message, num);
    Crypto cryptoInstance = Crypto(cryptoAddress);
    return cryptoInstance.sm3(data);
}
```

### keccak256Hash函数

将两个字符串连接到一起，再使用keccak256算法计算它的哈希值。

```solidity
function testKeccak256Hash(string memory str1, string memory str2) public view returns (bytes32) {
    bytes memory data = abi.encodePacked(str1, str2);
    Crypto cryptoInstance = Crypto(cryptoAddress);
    return cryptoInstance.keccak256Hash(data);
}
```

### sm2Verify函数

传入该协议中之前交互过的信息、公钥、签名 r 和 s ，验证该签名是否正确，并返回签名的账户地址。

```solidity
function testSm2Verify(bytes32 message, bytes memory publicKey, bytes32 r, bytes32 s) public view returns (bool, address) {
    Crypto cryptoInstance = Crypto(cryptoAddress);
    return cryptoInstance.sm2Verify(message, publicKey, r, s);
}
```

### curve25519VRFVerify函数

将给定的字符串用 UTF8 编码，然后使用 curve25519 VRF 签名算法和公钥对其进行签名，最后在智能合约中验证签名，并返回是否验证成功以及签名后的随机数。

```solidity
function testCurve25519VRFVerify(string memory input, string memory vrfPublicKey, string memory vrfProof) public view returns (bool, uint256) {
    Crypto cryptoInstance = Crypto(cryptoAddress);
    return cryptoInstance.curve25519VRFVerify(input, vrfPublicKey, vrfProof);
}
```



### 完整的合约

首先需要搭建好WeBASE-Front以及FISCO BCOS 2.8.0。

![](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051050302.webp)

```solidity
pragma solidity ^0.4.25;

import "./Crypto.sol";

contract CryptoTest{
    Crypto crypto;
    // 初始化构造函数实例化一个Crypto
    constructor() public {
        crypto = Crypto(0x5006);
    }
    // sm3函数使用
    function testSm3(string memory message, uint num) public view returns (bytes32) {
        bytes memory data = abi.encodePacked(message, num);
        return crypto.sm3(data);
    }
    // keccak256Hash函数
    function testKeccak256Hash(string memory str1, string memory str2) public view returns (bytes32) {
        bytes memory data = abi.encodePacked(str1, str2);
        return crypto.keccak256Hash(data);
    }
    // sm2Verify函数
    function testSm2Verify(bytes32 message, bytes memory publicKey, bytes32 r, bytes32 s) public view returns (bool, address) {
        return crypto.sm2Verify(message, publicKey, r, s);
    }
    // curve25519VRFVerify函数
    function testCurve25519VRFVerify(string memory input, string memory vrfPublicKey, string memory vrfProof) public view returns (bool, uint256) {
        return crypto.curve25519VRFVerify(input, vrfPublicKey, vrfProof);
    }    
}
```

部署CryptoTest合约。

![image-20230505105153585](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051051780.webp)

调用testSm3函数，计算它的SHA3/Keccak-256哈希。

![image-20230505113300203](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051133321.webp)

![image-20230505105231063](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051052170.webp)

调用testKeccak256Hash函数，使用keccak256算法计算它的哈希值。

![image-20230505105708175](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051057292.webp)

![image-20230505105741558](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305051057668.webp)

