/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.contract;

import com.webank.webase.front.base.SpringTestBase;
import com.webank.webase.front.contract.entity.wasm.AbiBinInfo;
import com.webank.webase.front.util.JsonUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class WasmServiceTest extends SpringTestBase {

    @Autowired
    WasmContractService wasmContractService;

    private static final String groupId = "group";
    private static final String contractName = "hello";
    private static final String source = "#![cfg_attr(not(feature = \"std\"), no_std)]\n" +
            "\n" +
            "use liquid::storage;\n" +
            "use liquid_lang as liquid;\n" +
            "\n" +
            "#[liquid::contract]\n" +
            "mod hello_world {\n" +
            "    use super::*;\n" +
            "\n" +
            "    #[liquid(storage)]\n" +
            "    struct HelloWorld {\n" +
            "        name: storage::Value<String>,\n" +
            "    }\n" +
            "\n" +
            "    #[liquid(methods)]\n" +
            "    impl HelloWorld {\n" +
            "        pub fn new(&mut self) {\n" +
            "            self.name.initialize(String::from(\"Alice\"));\n" +
            "        }\n" +
            "\n" +
            "        pub fn get(&self) -> String {\n" +
            "            self.name.clone()\n" +
            "        }\n" +
            "\n" +
            "        pub fn set(&mut self, name: String) {\n" +
            "            self.name.set(name)\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    #[cfg(test)]\n" +
            "    mod tests {\n" +
            "        use super::*;\n" +
            "\n" +
            "        #[test]\n" +
            "        fn get_works() {\n" +
            "            let contract = HelloWorld::new();\n" +
            "            assert_eq!(contract.get(), \"Alice\");\n" +
            "        }\n" +
            "\n" +
            "        #[test]\n" +
            "        fn set_works() {\n" +
            "            let mut contract = HelloWorld::new();\n" +
            "\n" +
            "            let new_name = String::from(\"Bob\");\n" +
            "            contract.set(new_name.clone());\n" +
            "            assert_eq!(contract.get(), \"Bob\");\n" +
            "        }\n" +
            "    }\n" +
            "}";


    @Test
    public void testCheck() {
        wasmContractService.checkLiquidEnv();
    }

    @Test
    public void testNewContract() {
        wasmContractService.execLiquidNewContract(groupId, contractName, source);
    }

    @Test
    public void testCompileLiquid() {
        AbiBinInfo abiBinInfo = wasmContractService.compileAndReturn(groupId, contractName);
        System.out.println(JsonUtils.objToString(abiBinInfo));
    }

}
