package com.mikitellurium.superflatbiomeextension.client;

import com.mikitellurium.superflatbiomeextension.registry.ModMultiNoiseBiomeSourceParameterLists;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatGeneratorConfig;
import com.mikitellurium.superflatbiomeextension.worldgen.DimensionSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

import java.util.Map;
import java.util.stream.Collectors;

public class FlatGeneratorsBuilder {
    // Recreate the world dimensions with the new configs
    public static WorldDimensions createDimensions(RegistryAccess lookup, WorldDimensions dimensions, EditCustomFlatLevelScreen.ConfigStorage configStorage) {
        Map<ResourceKey<LevelStem>, LevelStem> map = dimensions.dimensions().entrySet().stream()
                .filter((entry) -> entry.getValue().generator() instanceof CustomFlatChunkGenerator)
                .collect(Collectors.toMap(Map.Entry::getKey, (entry -> {
                            LevelStem levelStem = entry.getValue();
                            return createStem((CustomFlatChunkGenerator) levelStem.generator(), levelStem.type(), configStorage, lookup);
                        })
                ));
        return new WorldDimensions(map);
    }

    private static LevelStem createStem(CustomFlatChunkGenerator oldGenerator, Holder<DimensionType> dimensionType, EditCustomFlatLevelScreen.ConfigStorage configStorage, RegistryAccess lookup) {
        CustomFlatGeneratorConfig newConfig = createConfig(oldGenerator.getConfig(), configStorage);
        ChunkGenerator newGenerator = createFlatGenerator(newConfig, lookup);
        return new LevelStem(dimensionType, newGenerator);
    }

    private static CustomFlatGeneratorConfig createConfig(CustomFlatGeneratorConfig oldConfig, EditCustomFlatLevelScreen.ConfigStorage configStorage) {
        var config = new CustomFlatGeneratorConfig(
                oldConfig.getDimensionSettings(),
                oldConfig.getLayers(),
                configStorage.hasSurfaceFluid(),
                new CustomFlatGeneratorConfig.EnabledFeatures(
                        configStorage.hasVegetationFeatures(),
                        configStorage.hasTerrainFeatures(),
                        configStorage.hasLakeFeatures(),
                        configStorage.hasOreFeatures(),
                        configStorage.hasMiscFeatures()
                ),
                oldConfig.getBiomes(),
                oldConfig.getDensityFunctions(),
                oldConfig.getNoises()
        );
        config.setReduceUndergroundBiomes(configStorage.reduceUndergroundBiomes());
        return config;
    }

    private static ChunkGenerator createFlatGenerator(CustomFlatGeneratorConfig config, RegistryAccess registryAccess) {
        ResourceKey<MultiNoiseBiomeSourceParameterList> key;
        DimensionSettings settings = config.getDimensionSettings();
        if (settings.dimensionType() == BuiltinDimensionTypes.OVERWORLD && config.reduceUndergroundBiomes()) {
            key = ModMultiNoiseBiomeSourceParameterLists.REDUCED_UNDERGROUND_BIOMES;
        } else if (settings.biomeParameters().isPresent()) {
            key = settings.biomeParameters().get();
        } else if (settings.dimensionType() == BuiltinDimensionTypes.END) {
            return new CustomFlatChunkGenerator(TheEndBiomeSource.create(registryAccess.lookupOrThrow(Registries.BIOME)), config);
        } else {
            throw new IllegalStateException("Missing custom generator biome parameter list.");
        }
        Holder<MultiNoiseBiomeSourceParameterList> parameterList = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(key);
        return new CustomFlatChunkGenerator(MultiNoiseBiomeSource.createFromPreset(parameterList), config);
    }
}
