var Web3Utils = require('web3-utils');
const lang = localStorage.getItem('lang')
export function validate(type, value) {
    switch (type) {
        case 'address':
            var result = { is: Web3Utils.isAddress(value), msg: Web3Utils.isAddress(value) ? '' :'Invalid input: Unexpected end of address input ' };
            return result
            break;
        case 'bytes':
            var result = { is: Web3Utils.isHexStrict(value), msg: Web3Utils.isHexStrict(value) ? '' : 'Invalid input: Unexpected end of bytes input' };
            return result
            break;
        case 'bytes4':
            var result = { is: Web3Utils.isHexStrict(value), msg: Web3Utils.isHexStrict(value) ? '' : 'Invalid input: Unexpected end of bytes4 input' };
            return result
            break;
        case 'bytes32':
            var result = { is: Web3Utils.isHexStrict(value), msg: Web3Utils.isHexStrict(value) ? '' : 'Invalid input: Unexpected end of bytes32 input' };
            return result
            break;
        default:
            var result = { is: Web3Utils.isHexStrict(value), msg: Web3Utils.isHexStrict(value) ? '' : 'Invalid input: Unexpected end of '+type+' input' };
            return result
            break;
    }
}
export function stringToByte(str) {
    var bytes = new Array();
    var len, c;
    len = str.length;
    for (var i = 0; i < len; i++) {
        c = str.charCodeAt(i);
        if (c >= 0x010000 && c <= 0x10FFFF) {
            bytes.push(((c >> 18) & 0x07) | 0xF0);
            bytes.push(((c >> 12) & 0x3F) | 0x80);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if (c >= 0x000800 && c <= 0x00FFFF) {
            bytes.push(((c >> 12) & 0x0F) | 0xE0);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if (c >= 0x000080 && c <= 0x0007FF) {
            bytes.push(((c >> 6) & 0x1F) | 0xC0);
            bytes.push((c & 0x3F) | 0x80);
        } else {
            bytes.push(c & 0xFF);
        }
    }
    return bytes;
}
export function isString(str) {
    if (typeof str === 'string' || str instanceof String) {
        return true
    }
    return false
}
export function isBool(str) {
    if (!str) {
        return true;
    }
    var isBool = null;
    try {
        isBool = eval(str.toLowerCase())
        console.log(isBool, typeof isBool)
        if (typeof isBool ==='boolean'){
            isBool = true
        }else {
            isBool = false
        }
    } catch (error) {
        
        console.log(error)
    }
    return isBool
}
export function isLetter(str) {
    if(!str) {
        return true;
    }
    var reg = /^[A-Za-z0-9]+$/;
    return reg.test(str);
}
export function isUint(str) {
    if (!str) {
        return true;
    }
    try {
        var re = /^([0]|[1-9][0-9]*)$/;//判断字符串是否为数字//判断正整数/[1−9]+[0−9]∗]∗/ 
        console.log(re.test(str));
        return re.test(str)
        
    } catch (error) {
       
    }
}
export function isBytes(str) {
    
}
export function validateEvent(type, value) {
    var type = type
    let reg = /[0-9]+/g;

    var type = type.replace(reg, "");
    switch (type) {
        case 'address':
            var result = { is: Web3Utils.isAddress(value), msg: Web3Utils.isAddress(value) ? '' : lang == 'en' ? 'Invalid input: Unexpected end of address input ' : '输入address无效' };
            return result
            break;
        case 'bytes':
            var result = { is: Web3Utils.isHexStrict(value), msg: Web3Utils.isHexStrict(value) ? '' : lang == 'en' ? 'Invalid input: Unexpected end of bytes input' : '输入bytes无效' };
            return result
            break;
        case 'uint':
            var result = { is: isUint(value), msg: lang == 'en' ? 'Invalid input: Unexpected end of uint input' : '输入uint无效' };
            return result
            break;
        case 'string':
            var result = { is: isLetter(value), msg: lang == 'en' ? 'Invalid input: Unexpected end of string input' : '输入String无效' };
            return result
            break;
        case 'bool':
            var result = { is: isBool(value), msg: lang == 'en' ? 'Invalid input: Unexpected end of bool input' : '输入Bool无效' };
            return result
            break;
    }
}

export function isAddress(rule, value, callback) {
    if (value == '' || value == undefined || value == null) {
        callback();
    } else {
        if ((!Web3Utils.isAddress(value)) && value != '') {
            callback(new Error('请输入正确的地址'));
        } else {
            callback();
        }
    }
}