package com.mikitellurium.superflatbiomeextension.registry;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPreset;

public class ModWorldPresets {
    public static RegistryKey<WorldPreset> FLAT_BIOME_EXTENDED = of("flat_biome_extended");

    private static RegistryKey<WorldPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.ofVanilla(id));
    }
}
