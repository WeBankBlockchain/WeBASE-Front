var Web3Utils = require('web3-utils');

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
            
            break;
    }
}