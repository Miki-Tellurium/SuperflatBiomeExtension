package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.registry.ModSurfaceRuleProviders;
import com.mikitellurium.superflatbiomeextension.registry.NoiseSettingsRegistry;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.SurfaceRuleProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record DimensionSettings(
        ResourceKey<DimensionType> dimensionType,
        NoiseSettings noiseSettings,
        BlockState defaultBlock,
        BlockState defaultFluid,
        Holder<SurfaceRuleProvider> surfaceRule,
        Optional<ResourceKey<MultiNoiseBiomeSourceParameterList>> biomeParameters
) {
    private static final Map<ResourceKey<DimensionType>, DimensionSettings> DIMENSION_DATA_MAP = Util.make(new HashMap<>(), (map) -> {
        map.put(BuiltinDimensionTypes.OVERWORLD, new DimensionSettings(
                BuiltinDimensionTypes.OVERWORLD,
                NoiseSettingsRegistry.SURFACE,
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                ModSurfaceRuleProviders.REGISTRY.wrapAsHolder(ModSurfaceRuleProviders.OVERWORLD_FLAT),
                Optional.of(MultiNoiseBiomeSourceParameterLists.OVERWORLD)
        ));
        map.put(BuiltinDimensionTypes.NETHER, new DimensionSettings(
                BuiltinDimensionTypes.NETHER,
                NoiseSettingsRegistry.NETHER,
                Blocks.NETHERRACK.defaultBlockState(),
                Blocks.LAVA.defaultBlockState(),
                ModSurfaceRuleProviders.REGISTRY.wrapAsHolder(ModSurfaceRuleProviders.NETHER_FLAT),
                Optional.of(MultiNoiseBiomeSourceParameterLists.NETHER)
        ));
        map.put(BuiltinDimensionTypes.END, new DimensionSettings(
                BuiltinDimensionTypes.END,
                NoiseSettingsRegistry.END,
                Blocks.END_STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                ModSurfaceRuleProviders.REGISTRY.wrapAsHolder(ModSurfaceRuleProviders.END),
                Optional.empty()
        ));
        map.put(BuiltinDimensionTypes.OVERWORLD_CAVES, new DimensionSettings(
                BuiltinDimensionTypes.OVERWORLD_CAVES,
                NoiseSettingsRegistry.CAVES,
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                ModSurfaceRuleProviders.REGISTRY.wrapAsHolder(ModSurfaceRuleProviders.OVERWORLD_FLAT),
                Optional.empty()
        ));
    });
    public static final Codec<ResourceKey<DimensionType>> DIMENSION_TYPE_CODEC = ResourceKey.codec(Registries.DIMENSION_TYPE);
    public static final Codec<ResourceKey<MultiNoiseBiomeSourceParameterList>> BIOME_PARAMETERS_CODEC = ResourceKey.codec(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
    public static final Codec<DimensionSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DIMENSION_TYPE_CODEC.fieldOf("type").forGetter((settings) -> settings.dimensionType),
                    NoiseSettingsRegistry.CODEC.fieldOf("noise_settings").forGetter((settings) -> settings.noiseSettings),
                    BlockState.CODEC.fieldOf("default_block").forGetter((config) -> config.defaultBlock),
                    BlockState.CODEC.fieldOf("default_fluid").forGetter((config) -> config.defaultFluid),
                    SurfaceRuleProvider.CODEC.fieldOf("surface_rule").forGetter((config) -> config.surfaceRule),
                    BIOME_PARAMETERS_CODEC.optionalFieldOf("biome_parameters").forGetter((settings) -> settings.biomeParameters)
            ).apply(instance, DimensionSettings::new)
    );
    public static final Codec<DimensionSettings> CODEC = Codec.either(
            ResourceKey.codec(Registries.DIMENSION_TYPE), DIRECT_CODEC
    ).comapFlatMap(
            either -> either.map(
                    key -> {
                        DimensionSettings data = DIMENSION_DATA_MAP.get(key);
                        return data != null
                                ? DataResult.success(data)
                                : DataResult.error(() -> "No dimension data registered for key: " + key);
                    },
                    DataResult::success
            ),
            Either::right
    );

    public static DimensionSettings getSettings(ResourceKey<DimensionType> dimensionType) {
        return DIMENSION_DATA_MAP.get(dimensionType);
    }
}
