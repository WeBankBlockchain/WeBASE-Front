//package com.webank.webase.front.util;
//
//import java.nio.charset.StandardCharsets;
//import org.fisco.bcos.web3j.abi.datatypes.Address;
//import org.fisco.bcos.web3j.abi.datatypes.generated.Bytes32;
//import org.fisco.bcos.web3j.crypto.Credentials;
//import org.fisco.bcos.web3j.crypto.EncryptType;
//import org.fisco.bcos.web3j.crypto.RawTransaction;
//import org.fisco.bcos.web3j.crypto.TransactionEncoder;
//import org.fisco.bcos.web3j.crypto.gm.GenCredential;
//import org.fisco.bcos.web3j.utils.Numeric;
//import org.junit.Test;
//
//import java.math.BigInteger;
//import java.time.Duration;
//import java.time.Instant;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertSame;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;
//
//public class TransactionEncoderTest {
//
//   public static BigInteger gasPrice = BigInteger.ONE;
//   public static BigInteger gasLimit = BigInteger.TEN;
//
//    @Test
//    public void testSignMessage() {
//        byte[] signedMessage =
//                TransactionEncoder.signMessage(createContractTransaction(), SampleKeys.CREDENTIALS);
//        String hexMessage = Numeric.toHexString(signedMessage);
//        assertThat(
//                hexMessage,
//                is(
//                        "0xf85a8201f4010a8201f5840add5355887fffffffffffffff801ba01cf44d4680e1ecaf11a9a997b08055ae84c5d417b1fc7c2bdbaffc3fd4a7659aa07a424ef2ad019c599a24309c97f4cd10d0e4293a51d8c1abb095052bf54a7ba7"));
//    }
//
//
//    @Test
//    public void testGMSignMessage() {
//
//
//            Credentials credentials =
//                    GenCredential.create(
//                            "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f");
//
//            Instant startTime = Instant.now();
//
//            byte[] signedMessage = TransactionEncoder.signMessage(createContractTransaction(), credentials);
//            System.out.println("       sign  useTime: " + Duration.between(startTime, Instant.now()).toMillis());
//         //   String hexMessage = Numeric.toHexString(signedMessage);
//
//
//            // gm createTransaction!
//            EncryptType encryptType = new EncryptType(1);
//             assertSame(encryptType.getEncryptType(),1);
//            Credentials gmcredentials =
//                    GenCredential.create(
//                            "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f");
//
//            Instant startTime1 = Instant.now();
//             TransactionEncoder.signMessage(createContractTransaction(), gmcredentials);
//            System.out.println(" guomi sign  useTime: " + Duration.between(startTime1, Instant.now()).toMillis());
//      //      String hexMessage1 = Numeric.toHexString(signedMessage1);
//    }
//    @Test
//    public void testGMCrendetial() {
//
//        for (int i = 0; i < 10; i++) {
//
//            Instant systartTime = Instant.now();
//                    GenCredential.create(
//                            "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f"+i);
//            System.out.println("       siyao  useTime: " + Duration.between(systartTime, Instant.now()).toMillis());
//
//
//
//            // gm createTransaction!
//            EncryptType encryptType = new EncryptType(1);
//            assertSame(encryptType.getEncryptType(),1);
//            Instant gmsystartTime = Instant.now();
//
//            GenCredential.create(
//                            "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f"+i);
//            //     System.out.println(gmcredentials.getEcKeyPair().getPublicKey().toString(16));
//            System.out.println(encryptType+ "      gmsiyao  useTime: " + Duration.between(gmsystartTime, Instant.now()).toMillis());
//
//        }
//    }
////
//
//
//    private static RawTransaction createContractTransaction() {
//
//        BigInteger randomid = new BigInteger("500");
//        BigInteger blockLimit = new BigInteger("501");
//
//        return RawTransaction.createContractTransaction(
//                randomid, gasPrice, gasLimit, blockLimit, BigInteger.TEN, "0x0000000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
//    }
//
//    @Test
//    public void testAddress() {
//        System.out.println(Address.DEFAULT.getValue());
//    }
//
//
//    // expected: 0x3132330000000000000000000000000000000000000000000000000000000000
//    @Test
//    public void testBytes32() {
//        String input = "123";
//        String utf82Hex = Numeric.toHexStringNoPrefix(input.getBytes(StandardCharsets.UTF_8));
//        System.out.println("input " + input);
//        // expected: 0x313233
//        System.out.println("utf82Hex " + utf82Hex);
//
//        byte[] byteValue = input.getBytes();
//        byte[] byteValueUtf8 = utf82Hex.getBytes();
//
//        byte[] byteValueLen32 = new byte[32];
//        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
//        Bytes32 res1 = new Bytes32(byteValueLen32);
//        System.out.println("res1:");
//        System.out.println(Numeric.toHexString(res1.getValue()));
//
//        byte[] byteValueLen32_1 = new byte[32];
//        System.arraycopy(byteValueUtf8, 0, byteValueLen32_1, 0, byteValue.length);
//        Bytes32 res2 = new Bytes32(byteValueLen32);
//        System.out.println("res2:");
//        System.out.println(Numeric.toHexString(res2.getValue()));
//    }
//
//
//}
