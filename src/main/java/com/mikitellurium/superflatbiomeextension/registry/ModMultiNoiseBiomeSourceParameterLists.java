package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.ModBiomeNoiseSourcePresets;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class ModMultiNoiseBiomeSourceParameterLists {
    public static final RegistryKey<MultiNoiseBiomeSourceParameterList> OVERWORLD_FLAT = of("overworld_flat");

    private static RegistryKey<MultiNoiseBiomeSourceParameterList> of(String id) {
        return RegistryKey.of(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, FastId.ofMod(id));
    }

    public static void register() {
        MultiNoiseBiomeSourceParameterList.Preset.BY_IDENTIFIER.put(ModBiomeNoiseSourcePresets.OVERWORLD_FLAT.id(), ModBiomeNoiseSourcePresets.OVERWORLD_FLAT);
    }
}
