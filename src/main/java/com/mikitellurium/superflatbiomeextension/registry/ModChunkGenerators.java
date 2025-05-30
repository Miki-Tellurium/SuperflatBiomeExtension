package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModChunkGenerators {
    public static void register() {
        Registry.register(Registries.CHUNK_GENERATOR, FastId.ofMod("flat_biome_extended"), CustomFlatChunkGenerator.CODEC);
    }
}
