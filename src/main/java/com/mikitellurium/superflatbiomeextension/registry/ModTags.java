package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModTags {
    public static final TagKey<ConfiguredFeature<?, ?>> VEGETATION_FEATURES = feature("vegetation_features");
    public static final TagKey<ConfiguredFeature<?, ?>> TERRAIN_FEATURES = feature("terrain_features");
    public static final TagKey<ConfiguredFeature<?, ?>> LAKE_FEATURES = feature("lake_features");
    public static final TagKey<ConfiguredFeature<?, ?>> ORE_FEATURES = feature("ore_features");
    public static final TagKey<ConfiguredFeature<?, ?>> MISC_FEATURES = feature("misc_features");
    public static final TagKey<ConfiguredFeature<?, ?>> ALWAYS_GENERATE_FEATURES = feature("always_generate_features");
    public static final TagKey<ConfiguredFeature<?, ?>> NEVER_GENERATE_FEATURES = feature("never_generate_features");

    public static TagKey<ConfiguredFeature<?, ?>> feature(String name) {
        return TagKey.create(Registries.CONFIGURED_FEATURE, FastId.ofMod(name));
    }
}