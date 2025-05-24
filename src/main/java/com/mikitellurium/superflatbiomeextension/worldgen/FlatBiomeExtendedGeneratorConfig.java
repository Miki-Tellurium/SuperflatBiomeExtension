package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.mixin.DensityFunctionsAccessor;
import com.mikitellurium.superflatbiomeextension.mixin.GenerationShapeConfigAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FlatBiomeExtendedGeneratorConfig {
    public static final Codec<FlatBiomeExtendedGeneratorConfig> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codec.INT.fieldOf("world_height").forGetter((config) -> config.height),
                    Codec.BOOL.fieldOf("generate_water").forGetter((config) -> config.generateWater),
                    Codec.BOOL.fieldOf("has_lava_lakes").forGetter((config) -> config.lavaLakes),
                    Codec.BOOL.fieldOf("generate_ores").forGetter((config) -> config.generateOres),
                    Codec.BOOL.fieldOf("has_features").forGetter((config) -> config.hasFeatures),
                    Codec.BOOL.fieldOf("has_structures").forGetter((config) -> config.hasStructures),
                    RegistryOps.getEntryCodec(MiscPlacedFeatures.DESERT_WELL)
            ).apply(instance, FlatBiomeExtendedGeneratorConfig::new)
    );
    private final ChunkGeneratorSettings settings;
    private final int height;
    private final int surfaceY;
    private final boolean generateWater;
    private final boolean lavaLakes;
    private final boolean generateOres;
    private final boolean hasFeatures;
    private final boolean hasStructures;
    private final Map<Integer, FeatureStepCheck> featureChecks;
    private final RegistryEntry<PlacedFeature> feature;

    public FlatBiomeExtendedGeneratorConfig(int height, boolean generateWater, boolean lavaLakes, boolean generateOres, boolean hasFeatures, boolean hasStructures, RegistryEntry<PlacedFeature> feature) {
        this.height = height;
        this.surfaceY = -64 + height;
        this.generateWater = generateWater;
        this.lavaLakes = lavaLakes;
        this.generateOres = generateOres;
        this.hasFeatures = hasFeatures;
        this.hasStructures = hasStructures;
        this.feature = feature;
        this.settings = this.createDefaultSettings();
        this.featureChecks = Map.of(
                GenerationStep.Feature.LAKES.ordinal(), new FeatureStepCheck(this.lavaLakes, GenerationStep.Feature.LAKES),
                GenerationStep.Feature.UNDERGROUND_ORES.ordinal(), new FeatureStepCheck(this.generateOres, GenerationStep.Feature.UNDERGROUND_ORES),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal(), new FeatureStepCheck(this.hasStructures, GenerationStep.Feature.UNDERGROUND_STRUCTURES),
                GenerationStep.Feature.SURFACE_STRUCTURES.ordinal(), new FeatureStepCheck(this.hasStructures, GenerationStep.Feature.SURFACE_STRUCTURES),
                GenerationStep.Feature.STRONGHOLDS.ordinal(), new FeatureStepCheck(this.hasStructures, GenerationStep.Feature.STRONGHOLDS)
        );
    }

    public ChunkGeneratorSettings getSettings() {
        return settings;
    }

    public GenerationSettings createGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        GenerationSettings.Builder builder = new GenerationSettings.Builder();
        GenerationSettings generationSettings = biomeEntry.value().getGenerationSettings();
        List<RegistryEntryList<PlacedFeature>> list = generationSettings.getFeatures();
        for (int i = 0; i < list.size(); i++) {
            if ((this.hasFeatures && !featureChecks.containsKey(i)) || this.featureChecks.get(i).test(i))
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
                ModSurfaceRules.createDefaultModSurfaceRule(this.surfaceY, this.generateWater),
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
        return generateWater;
    }

    public boolean hasLavaLakes() {
        return lavaLakes;
    }

    public boolean generateOres() {
        return generateOres;
    }

    public boolean hasFeatures() {
        return hasFeatures;
    }

    public boolean hasStructures() {
        return hasStructures;
    }

    private record FeatureStepCheck(Boolean bool, GenerationStep.Feature featureStep) implements Predicate<Integer> {
        @Override
        public boolean test(Integer i) {
            return bool && i == featureStep.ordinal();
        }
    }
}
