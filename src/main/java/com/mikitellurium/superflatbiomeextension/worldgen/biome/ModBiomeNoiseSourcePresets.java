package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.google.common.collect.ImmutableList;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import java.util.function.Function;

public class ModBiomeNoiseSourcePresets {
    public static final MultiNoiseBiomeSourceParameterList.Preset REDUCED_UNDERGROUND_BIOMES = new MultiNoiseBiomeSourceParameterList.Preset(
            FastId.ofMod("reduced_underground_biomes"), ModBiomeNoiseSourcePresets::generateOverworldBiomes);

    private static <T> Climate.ParameterList<T> generateOverworldBiomes(final Function<ResourceKey<Biome>, T> lookup) {
        ImmutableList.Builder<Pair<Climate.ParameterPoint, T>> builder = ImmutableList.builder();
        new ReducedUndergroundBiomesBuilder().addBiomes(p -> builder.add(p.mapSecond(lookup)));
        return new Climate.ParameterList<>(builder.build());
    }
}
