package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.mikitellurium.superflatbiomeextension.registry.ModSurfaceRuleProviders;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

public interface SurfaceRuleProvider {
    Codec<Holder<SurfaceRuleProvider>> CODEC = RegistryFixedCodec.create(ModSurfaceRuleProviders.REGISTRY_KEY);

    SurfaceRules.RuleSource apply(HolderGetter<Biome> biomes, int surfaceY, boolean generateWater);
}
