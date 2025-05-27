package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.google.common.collect.ImmutableList;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.function.Function;

public class ModBiomeNoiseSourcePresets {
    public static final MultiNoiseBiomeSourceParameterList.Preset OVERWORLD_FLAT = new MultiNoiseBiomeSourceParameterList.Preset(
            FastId.ofMod("overworld_flat"), ModBiomeNoiseSourcePresets::getOverworldEntries
    );

    private static <T> MultiNoiseUtil.Entries<T> getOverworldEntries(Function<RegistryKey<Biome>, T> biomeEntryGetter) {
        ImmutableList.Builder<Pair<MultiNoiseUtil.NoiseHypercube, T>> builder = ImmutableList.builder();
        new FlatBiomeParameters().writeOverworldBiomeParameters((pair) -> builder.add(pair.mapSecond(biomeEntryGetter)));
        return new MultiNoiseUtil.Entries<>(builder.build());
    }
}
