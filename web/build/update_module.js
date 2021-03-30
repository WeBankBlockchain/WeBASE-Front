const fs = require('fs');
const path = require('path');

const aceWebpackResolve = './node_modules/ace-builds/webpack-resolver.js';
const requireFromString = './node_modules/require-from-string/index.js';
try {
  // 修改ace-builds webpack-resolver打包路径
  let fileContent = fs.readFileSync(aceWebpackResolve, 'utf-8');
  if (!fileContent) {
    console.log("ace-builds webpack-resolver not find")
    return
  }
  let oldStr = /esModule=false!/g
  let newStr = 'esModule=false&outputPath=ace/!'
  fileContent = fileContent.replace(oldStr, newStr);
  fs.writeFileSync(aceWebpackResolve, fileContent, 'utf8');
  console.log('ace-builds webpack-resolver 打包路径修改成功')
} catch (error) {
  console.log('error: ' + error)
}

try {
  // 修改require-from-string index的写法问题
  let fileContent2 = fs.readFileSync(requireFromString, 'utf-8');
  if (!fileContent2) {
    console.log("require-from-string not find")
    return
  }
  let oldStr = 'var Module = require("module")'
  let newStr = 'var Module = module.constructor'
  fileContent2 = fileContent2.replace(oldStr, newStr);
  fs.writeFileSync(requireFromString, fileContent2, 'utf8');
  console.log('require-from-string 依赖修改成功')
} catch (error) {
  console.log('error: ' + error)
}