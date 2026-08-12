package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.mixin.MultiNoiseBiomeSourceParameterListPresetAccessor;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.ModBiomeNoiseSourcePresets;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

public class ModMultiNoiseBiomeSourceParameterLists {
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> REDUCED_UNDERGROUND_BIOMES = of("reduced_underground_biomes");

    private static ResourceKey<MultiNoiseBiomeSourceParameterList> of(String id) {
        return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, FastId.ofMod(id));
    }

    public static void register() {
        MultiNoiseBiomeSourceParameterListPresetAccessor.getBY_NAME()
                .put(ModBiomeNoiseSourcePresets.REDUCED_UNDERGROUND_BIOMES.id(), ModBiomeNoiseSourcePresets.REDUCED_UNDERGROUND_BIOMES);
    }
}
