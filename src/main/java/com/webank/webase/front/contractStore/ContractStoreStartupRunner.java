package com.webank.webase.front.contractStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ContractStoreStartupRunner implements CommandLineRunner {

    @Autowired
    PresetDataService presetDataService;

    @Override
    public void run(String... args) throws Exception {
        presetDataService.initPresetData();
    }
}

