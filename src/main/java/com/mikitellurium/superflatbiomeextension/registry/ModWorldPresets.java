package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class ModWorldPresets {
    public static final ResourceKey<WorldPreset> FLAT_BIOME_EXTENDED = ResourceKey.create(Registries.WORLD_PRESET, FastId.ofMod("flat_biome_extended"));
}
