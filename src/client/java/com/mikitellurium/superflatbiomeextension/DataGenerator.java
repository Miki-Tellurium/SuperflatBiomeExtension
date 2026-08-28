package com.mikitellurium.superflatbiomeextension;

import com.mikitellurium.superflatbiomeextension.generator.ModTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class DataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        CompletableFuture<HolderLookup.Provider> lookup = generator.getRegistries();

        pack.addProvider((FabricPackOutput output) -> new ModTagProvider.FeatureProvider(output, lookup));
    }
}