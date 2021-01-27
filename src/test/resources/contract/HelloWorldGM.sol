pragma solidity ^0.4.24;

contract HelloWorldGM{
    string name;

    constructor() public{
       name = "Hello, World!";
    }

    function get() constant public returns(string){
        return name;
    }

    function set(string n) public{
        name = n;
    }
}
