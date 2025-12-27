package org.whitelist_watcher.whitelist_watcher.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class Whitelist_watcherDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        //Nothing in it,because this is a mod for server only.
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
    }
}
