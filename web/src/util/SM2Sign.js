// var sm2 = require('./sm_sm2');
var sm3 = require('./sm_sm3');

// function signRS(ecprvhex,msg){
//     var keyPair = sm2.SM2KeyPair(null,ecprvhex);
//     var pubKeyHex = keyPair.pub.getX().toString(16)+keyPair.pub.getY().toString(16);
//     var _msg = Array.from(msg);

//     var signData= keyPair.sign(_msg);
//     //console.log("pubKeyHexLen:",pubKeyHex.length," pubKeyHex:",pubKeyHex);
//     //console.log("sign.R:",signData.r," rLen:",signData.r.length," sLen:",signData.s.length," sign.S:",signData.s);

//     var rHex = "000000000000000000000" + signData.r;
//     var sHex = "000000000000000000000" + signData.s;
//     var rHexLen = rHex.length - 64;
//     var sHexLen = sHex.length - 64;
//     rHex = rHex.substr(rHexLen,64);
//     sHex = sHex.substr(sHexLen,64);

//     var r = new Buffer(rHex,'hex');
//     var s = new Buffer(sHex,'hex');
//     var pub = new Buffer(pubKeyHex, 'hex');
//     return {'r': r, 's': s,'pub':pub};
// }

// function priToPub(ecprvhex){
//     var keyPair = sm2.SM2KeyPair(null,ecprvhex);
//     var pubKeyHex = keyPair.pub.getX().toString(16)+keyPair.pub.getY().toString(16);
//     return new Buffer(pubKeyHex,'hex');
// }

function sm3Digest(msg){
    var _sm3 = new sm3();
    var rawData = Array.from(msg);
    var digest = _sm3.sum(rawData);
    var hashHex = Array.from(digest, function(byte) {return ('0' + (byte & 0xFF).toString(16)).slice(-2);}).join('');
    // console.log(rawData,digest,hashHex)
    return hashHex;
   
}

// /**
//  * 国密摘要算法（SM3）
//  * @param str:raw string
//  * @return the 256-bit hex string produced by SM3 from a raw string
//  */
//  function sm3Digest(str) {
//     //1. 转换为二进制数组
//     var binArr = str2bin(str2rstr_utf8(str));
//     //2. 填充
//     var groupNum = alignSM3(binArr, str.length);
//     //3. 迭代压缩
//     var v = new Array(8);//初始值
//     v[0] = 0x7380166f;
//     v[1] = 0x4914b2b9;
//     v[2] = 0x172442d7;
//     v[3] = 0xda8a0600;
//     v[4] = 0xa96f30bc;
//     v[5] = 0x163138aa;
//     v[6] = 0xe38dee4d;
//     v[7] = 0xb0fb0e4e;
//     //按 512bit 分组进行压缩
//     for (var i = 0; i < groupNum; i++) {
//         v = compress(v, binArr, i);
//     }
//     return word2str(v, '');
// }
 
// /**
//  * 将数组转换为字符串。数组长度不定，每个元素为 32bit 的数字。
//  * @param words:数组，每个元素为 32bit 的数字
//  * @param seperator:在每个数组元素转换得到的字符串之间的分隔符
//  */
// function word2str(words, seperator) {
//     var prefix = Array(8).join('0');
//     for (var i = 0; i < words.length; i++) {
//         //若 hex 不足 8 位，则高位补 0
//         words[i] = (prefix + (words[i] >>> 0).toString(16)).slice(-8);
//     }
 
//     return words.join(seperator);
// }
 
// /**
//  * 将字符串转换为二进制数组，默认字符串编码为 UTF-8，且范围在 0x00~0xFF 内。
//  * 若某些字符的编码超过此范围，则会只保留最低字节。加密可正常进行，但加密结果有误。
//  * 每个数组元素包含 4 个字符，即 32 bit。
//  * @param 字符串
//  * @return 数组，长度为（字符串长度 / 4），每个元素为 32bit 的数字
//  */
// function str2bin(str) {
//     var binary = new Array(str.length >> 2);
//     for (var i = 0; i < str.length * 8; i += 8) {
//         binary[i >> 5] |= (str.charCodeAt(i / 8) & 0xFF) << (24 - i % 32);
//     }
//     return binary;
// }
 
// /**
//  * 对明文的二进制串进行填充
//  * <pre>
//  * |  满足 mod 512 = 448 |           固定 64 位         |
//  * | 明文二进制 |填充部分|明文二进制串的长度的二进制表示|
//  *  xxxxxxxxxxxx 10.....0 0...........................xx
//  * </pre>
//  * @param arr:数组，每个元素为 32bit 的数字
//  * @param strLen：明文字符串长度
//  * @return 数组，每个元素为 32bit 的数字，数组长度为 16 的倍数（包括 16）
//  */
// function alignSM3(arr, strLen) {
//     //在明文二进制串后面拼接 1000 0000
//     arr[strLen >> 2] |= 0x80 << (24 - strLen % 4 * 8);
//     var groupNum = ((strLen + 8) >> 6) + 1;//以 512bit 为一组，总的组数
//     var wordNum = groupNum * 16;//一个 word 32bit，总的 word 数
 
//     for (var i = (strLen >> 2) + 1; i < wordNum; i++) {
//         arr[i] = 0;
//     }
//     arr[wordNum - 1] = strLen * 8;//在末尾填上明文的二进制长度
 
//     return groupNum;
// }
 
// /**
//  * 压缩函数中的置换函数
//  */
// function p0(x) {
//     return x ^ bitRol(x, 9) ^ bitRol(x, 17);
// }
 
// /**
//  * 压缩函数中的置换函数
//  */
// function p1(x) {
//     return x ^ bitRol(x, 15) ^ bitRol(x, 23);
// }
 
// /**
//  * 循环左移
//  */
// function bitRol(input, n) {
//     return (input << n) | (input >>> (32 - n));
// }
 
// /**
//  * 压缩函数
//  */
// function compress(v, binArr, i) {
//     //将消息分组扩展成 132 个字
//     var w1 = new Array(68);
//     var w2 = new Array(64);
//     for (var j = 0; j < 68; j++) {
//         if (j < 16) {
//             w1[j] = binArr[i * 16 + j];
//         } else {
//             w1[j] = p1(w1[j-16] ^ w1[j-9] ^ bitRol(w1[j-3], 15)) ^ bitRol(w1[j-13], 7) ^ w1[j-6];
//         }
//     }
//     for (var j = 0; j < 64; j++) {
//         w2[j] = w1[j] ^ w1[j+4];
//     }
 
//     //压缩
//     var a = v[0];
//     var b = v[1];
//     var c = v[2];
//     var d = v[3];
//     var e = v[4];
//     var f = v[5];
//     var g = v[6];
//     var h = v[7];
//     var ss1;
//     var ss2;
//     var tt1;
//     var tt2;
//     for (var j = 0; j < 64; j++) {
//         ss1 = bitRol(addAll(bitRol(a, 12) , e , bitRol(t(j), j)), 7);
//         ss2 = ss1 ^ bitRol(a, 12);
//         tt1 = addAll(ff(a, b, c, j) , d , ss2 , w2[j]);
//         tt2 = addAll(gg(e, f, g, j) , h , ss1 , w1[j]);
//         d = c;
//         c = bitRol(b, 9);
//         b = a;
//         a = tt1;
//         h = g;
//         g = bitRol(f, 19);
//         f = e;
//         e = p0(tt2);
//     }
//     v[0] ^= a;
//     v[1] ^= b;
//     v[2] ^= c;
//     v[3] ^= d;
//     v[4] ^= e;
//     v[5] ^= f;
//     v[6] ^= g;
//     v[7] ^= h;
//     return v;
// }
 
// /**
//  * 常量 T 随 j 的不同而不同
//  */
// function t(j) {
//     if (0 <= j && j < 16) {
//         return 0x79CC4519;
//     } else if (j < 64) {
//         return 0x7A879D8A;
//     }
// }
 
// /**
//  * 布尔函数，随 j 的变化取不同的表达式
//  */
// function ff(x, y, z, j) {
//     if (0 <= j && j < 16) {
//         return x ^ y ^ z;
//     } else if (j < 64) {
//         return (x & y) | (x & z) | (y & z);
//     }
// }
 
// /**
//  * 布尔函数，随 j 的变化取不同的表达式
//  */
// function gg(x, y, z, j) {
//     if (0 <= j && j < 16) {
//         return x ^ y ^ z;
//     } else if (j < 64) {
//         return (x & y) | (~x & z);
//     }
// }
 
// /**
//  * 两数相加
//  * 避免某些 js 引擎的 32 位加法的 bug
//  */
// function safe_add(x, y) {
//     var lsw = ( x & 0xFFFF ) + (y & 0xFFFF);
//     var msw = ( x >> 16 ) + (y >> 16) + (lsw >> 16);
//     return (msw << 16) | ( lsw & 0xFFFF );
// }
 
// /**
//  * 将所有参数相加
//  */
// function addAll() {
//     var sum = 0;
//     for (var i = 0; i < arguments.length; i++) {
//         sum = safe_add(sum, arguments[i]);
//     }
//     return sum;
// }
 
// /**
//  * UTF-16 --> UTF-8
//  */
// function str2rstr_utf8(input) {
//     var output = "" ;
//     var i = -1 ;
//     var x, y ;
 
//     while(++ i < input.length) {
//         //按 UTF-16 解码
//         x = input.charCodeAt(i);
//         y = i + 1 < input.length ? input .charCodeAt (i + 1) : 0 ;
//         if( 0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF ) {
//             x = 0x10000 + ((x & 0x03FF) << 10 ) + (y & 0x03FF);
//             i++;
//         }
 
//         //按 UTF-8 编码
//         if( x <= 0x7F ) {
//             output += String.fromCharCode(x);
//         }
//         else if(x <= 0x7FF) {
//             output += String.fromCharCode(
//                 0xC0 | ((x >>> 6 ) & 0x1F),
//                 0x80 | ( x         & 0x3F ));
//         } else if(x <= 0xFFFF) {
//             output += String.fromCharCode(
//                 0xE0 | ((x >>> 12) & 0x0F ),
//                 0x80 | ((x >>> 6 ) & 0x3F),
//                 0x80 | ( x         & 0x3F ));
//         } else if(x <= 0x1FFFFF) {
//             output += String.fromCharCode(
//                 0xF0 | ((x >>> 18) & 0x07 ),
//                 0x80 | ((x >>> 12) & 0x3F),
//                 0x80 | ((x >>> 6 ) & 0x3F),
//                 0x80 | ( x         & 0x3F ));
//         }
//     }
//     return output;
// }

exports.sm3Digest = sm3Digest;
// exports.signRS = signRS;
// exports.priToPub = priToPub;

/*var hashData = sm3Digest("trans()");
console.log("hashData:",hashData);

var pri = "c9a497f262b30acc933891257c5652f04d2de8b01bd0fbf939e497f215f5394f";
var pub = "04756185a0cd1a3b240bd8fd400b0f34083f557cb3a2a80f0cfb8f9f093f21ed8e349908e3d641db6fe43b152d1cb2180834a398d5e3314403d01178339b6447af";
var msg = "123456";
var signData = signRS(pri,msg);
var verifyRS = verifyRS(pub,msg,signData);
console.log("verifyRSResult:",verifyRS);*/

/*var genKey = sm2GenKey("sm2");
var pri = genKey.ecprvhex;
var pub = genKey.ecpubhex;
console.log("genpri:" + pri + " genpub:" + pub);

var pri = "c9a497f262b30acc933891257c5652f04d2de8b01bd0fbf939e497f215f5394f";
var pub = "04756185a0cd1a3b240bd8fd400b0f34083f557cb3a2a80f0cfb8f9f093f21ed8e349908e3d641db6fe43b152d1cb2180834a398d5e3314403d01178339b6447af";
var msg = "123456";

var _sign = sm2Sign(pri,msg);
var lresult = verify(pub,msg,_sign);
console.log("verifyResult:" + lresult);

var hashData = sm3Digest(msg);

console.log("sm3HashData:" + hashData);
console.log("address:" + hashData.substr(0,40));*/