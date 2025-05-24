package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.SuperflatBiomeExtension;
import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModChunkGenerators {
    public static void register() {
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(SuperflatBiomeExtension.modId(), "flat_biome_extended"), FlatBiomeExtendedChunkGenerator.CODEC);
    }
}
