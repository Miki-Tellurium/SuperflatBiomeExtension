package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.mixin.DensityFunctionsAccessor;
import com.mikitellurium.superflatbiomeextension.mixin.GenerationShapeConfigAccessor;
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
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FlatBiomeExtendedGeneratorConfig {
    public static final Codec<FlatBiomeExtendedGeneratorConfig> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codec.INT.fieldOf("world_height").forGetter((config) -> config.height),
                    Codec.BOOL.fieldOf("generate_water").forGetter((config) -> config.generateWater),
                    Codec.BOOL.fieldOf("has_features").forGetter((config) -> config.hasFeatures),
                    Codec.BOOL.fieldOf("has_lava_lakes").forGetter(FlatBiomeExtendedGeneratorConfig::hasLavaLakes),
                    Codec.BOOL.fieldOf("generate_ores").forGetter(FlatBiomeExtendedGeneratorConfig::generateOres),
                    Codec.BOOL.fieldOf("has_structures").forGetter(FlatBiomeExtendedGeneratorConfig::hasStructures)
            ).apply(instance, FlatBiomeExtendedGeneratorConfig::new)
    );
    private final int height;
    private final int surfaceY;
    private final boolean generateWater;
    private final boolean hasFeatures;
    private final Map<Integer, FeatureStepCheck> featureChecks;
    private final ChunkGeneratorSettings settings;

    public FlatBiomeExtendedGeneratorConfig(int height, boolean generateWater, boolean hasFeatures, boolean lavaLakes, boolean generateOres, boolean hasStructures) {
        this.height = height;
        this.surfaceY = -64 + height;
        this.generateWater = generateWater;
        this.hasFeatures = hasFeatures;
        this.featureChecks = Map.of(
                GenerationStep.Feature.LAKES.ordinal(), new FeatureStepCheck(lavaLakes, GenerationStep.Feature.LAKES),
                GenerationStep.Feature.UNDERGROUND_ORES.ordinal(), new FeatureStepCheck(generateOres, GenerationStep.Feature.UNDERGROUND_ORES),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.UNDERGROUND_STRUCTURES),
                GenerationStep.Feature.SURFACE_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.SURFACE_STRUCTURES),
                GenerationStep.Feature.STRONGHOLDS.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Feature.STRONGHOLDS)
        );
        this.settings = this.createDefaultSettings();
    }

    public ChunkGeneratorSettings getSettings() {
        return settings;
    }

    public GenerationSettings createGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        GenerationSettings.Builder builder = new GenerationSettings.Builder();
        GenerationSettings generationSettings = biomeEntry.value().getGenerationSettings();
        List<RegistryEntryList<PlacedFeature>> list = generationSettings.getFeatures();
        for (int i = 0; i < list.size(); i++) {
            if ((this.hasFeatures() && !featureChecks.containsKey(i)) || this.featureChecks.get(i).test(i))
                for (RegistryEntry<PlacedFeature> feature : list.get(i)) {
                    builder.addFeature(i, feature);
                }
        }
        return builder.build();
    }

    private ChunkGeneratorSettings createDefaultSettings() {
        RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();
        RegistryEntryLookup<DensityFunction> densityFunctionRegistry = lookup.getOrThrow(RegistryKeys.DENSITY_FUNCTION);
        RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry = lookup.getOrThrow(RegistryKeys.NOISE_PARAMETERS);
        return new ChunkGeneratorSettings(
                GenerationShapeConfigAccessor.getSURFACE(),
                Blocks.STONE.getDefaultState(),
                Blocks.WATER.getDefaultState(),
                DensityFunctionsAccessor.invokeCreateSurfaceNoiseRouter(densityFunctionRegistry, noiseParametersRegistry, false, false),
                ModSurfaceRules.createDefaultModSurfaceRule(this.surfaceY, this.generateWater()),
                new VanillaBiomeParameters().getSpawnSuitabilityNoises(),
                this.surfaceY - 1,
                false,
                false,
                true,
                false
        );
    }

    public int getHeight() {
        return height;
    }

    public boolean generateWater() {
        return this.generateWater;
    }

    public boolean hasFeatures() {
        return this.hasFeatures;
    }

    public boolean hasLavaLakes() {
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

    private record FeatureStepCheck(boolean isEnabled, GenerationStep.Feature featureStep) implements Predicate<Integer> {
        @Override
        public boolean test(Integer i) {
            return isEnabled && i == featureStep.ordinal();
        }
    }
}
