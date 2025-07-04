package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.mixin.DensityFunctionsAccessor;
import com.mikitellurium.superflatbiomeextension.registry.GenerationShapeConfigRegistry;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.FlatBiomeParameters;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.ModSurfaceRules;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CustomFlatGeneratorConfig {
    public static final Codec<CustomFlatGeneratorConfig> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    GenerationShapeConfigRegistry.CODEC.optionalFieldOf("shape_config", GenerationShapeConfigRegistry.SURFACE).forGetter((config) -> config.shapeConfig),
                    Codec.INT.fieldOf("layer_count").forGetter((config) -> config.layerCount),
                    Codec.BOOL.fieldOf("generate_water").forGetter((config) -> config.generateWater),
                    Codec.BOOL.fieldOf("has_features").forGetter((config) -> config.hasFeatures),
                    Codec.BOOL.fieldOf("has_structures").forGetter(CustomFlatGeneratorConfig::hasStructures),
                    Codec.BOOL.fieldOf("has_lava_lakes").forGetter(CustomFlatGeneratorConfig::hasLakes),
                    Codec.BOOL.fieldOf("generate_ores").forGetter(CustomFlatGeneratorConfig::generateOres)
            ).apply(instance, CustomFlatGeneratorConfig::new)
    );
    private final GenerationShapeConfig shapeConfig;
    private final int layerCount;
    private final boolean generateWater;
    private final boolean hasFeatures;
    private final Map<Integer, FeatureStepCheck> featureChecks;
    private final ChunkGeneratorSettings settings;

    public CustomFlatGeneratorConfig(GenerationShapeConfig shapeConfig, int layerCount, boolean generateWater, boolean hasFeatures, boolean hasStructures, boolean hasLakes, boolean generateOres) {
        validateLayerCount(layerCount);
        this.shapeConfig = shapeConfig;
        this.layerCount = layerCount;
        this.generateWater = generateWater;
        this.hasFeatures = hasFeatures;
        this.featureChecks = Map.of(
                GenerationStep.Feature.LAKES.ordinal(), new FeatureStepCheck(hasLakes, GenerationStep.Feature.LAKES),
                GenerationStep.Feature.UNDERGROUND_ORES.ordinal(), new FeatureStepCheck(generateOres, GenerationStep.Feature.UNDERGROUND_ORES),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.UNDERGROUND_STRUCTURES),
                GenerationStep.Feature.SURFACE_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.SURFACE_STRUCTURES),
                GenerationStep.Feature.STRONGHOLDS.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.STRONGHOLDS)
        );
        this.settings = this.createSettings(shapeConfig.minimumY() + layerCount);
    }

    public ChunkGeneratorSettings getChunkGeneratorSettings() {
        return settings;
    }

    public GenerationSettings createGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        GenerationSettings.Builder builder = new GenerationSettings.Builder();
        GenerationSettings generationSettings = biomeEntry.value().getGenerationSettings();
        List<RegistryEntryList<PlacedFeature>> list = generationSettings.getFeatures();
        for (int i = 0; i < list.size(); i++) {
            if ((this.hasFeatures() && !featureChecks.containsKey(i))
                    || (this.featureChecks.containsKey(i) && this.featureChecks.get(i).test(i)))
                for (RegistryEntry<PlacedFeature> feature : list.get(i)) {
                    builder.addFeature(i, feature);
                }
        }
        return builder.build();
    }

    private ChunkGeneratorSettings createSettings(int surfaceY) {
        RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();
        RegistryEntryLookup<DensityFunction> densityFunctionRegistry = lookup.getOrThrow(RegistryKeys.DENSITY_FUNCTION);
        RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry = lookup.getOrThrow(RegistryKeys.NOISE_PARAMETERS);
        return new ChunkGeneratorSettings(
                this.shapeConfig,
                Blocks.STONE.getDefaultState(),
                Blocks.WATER.getDefaultState(),
                DensityFunctionsAccessor.invokeCreateSurfaceNoiseRouter(densityFunctionRegistry, noiseParametersRegistry, false, false),
                ModSurfaceRules.createDefaultModSurfaceRule(surfaceY, this.generateWater()),
                new FlatBiomeParameters().getSpawnSuitabilityNoises(),
                surfaceY - 1,
                false,
                false,
                true,
                false
        );
    }

    public GenerationShapeConfig getGenerationShapeConfig() {
        return this.shapeConfig;
    }

    public int getLayerCount() {
        return this.layerCount;
    }

    public boolean generateWater() {
        return this.generateWater;
    }

    public boolean hasFeatures() {
        return this.hasFeatures;
    }

    public boolean hasLakes() {
        return featureChecks.get(GenerationStep.Feature.LAKES.ordinal()).isEnabled();
    }

    public boolean generateOres() {
        return featureChecks.get(GenerationStep.Feature.UNDERGROUND_ORES.ordinal()).isEnabled();
    }

    public boolean hasStructures() {
        return featureChecks.get(GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal()).isEnabled() ||
                featureChecks.get(GenerationStep.Feature.SURFACE_STRUCTURES.ordinal()).isEnabled() ||
                featureChecks.get(GenerationStep.Feature.STRONGHOLDS.ordinal()).isEnabled();
    }

    private static void validateLayerCount(int layerCount) {
        if (layerCount < 1 || layerCount > 384) {
            throw new IllegalArgumentException("Layer count must be between 1 and 384. Got: " + layerCount);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static CustomFlatGeneratorConfig createDefault() {
        return new CustomFlatGeneratorConfig(
                GenerationShapeConfigRegistry.SURFACE,
                64,
                true,
                true,
                true,
                false,
                false
        );
    }

    private record FeatureStepCheck(boolean isEnabled, GenerationStep.Feature featureStep) implements Predicate<Integer> {
        @Override
        public boolean test(Integer i) {
            return isEnabled && i == featureStep.ordinal();
        }
    }
}
