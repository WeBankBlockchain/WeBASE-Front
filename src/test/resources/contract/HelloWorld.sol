pragma solidity ^0.4.25;

contract HelloWorld
{
    uint256[] ua;
    function set(uint256[] memory _ua) public {
        ua = _ua;
    }

    function get() view public returns(uint256[]) {
        return ua;
    }
}