package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPreset;

public class ModWorldPresets {
    public static final RegistryKey<WorldPreset> FLAT_BIOME_EXTENDED = of("flat_biome_extended");

    private static RegistryKey<WorldPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.WORLD_PRESET, FastId.ofMod(id));
    }
}
