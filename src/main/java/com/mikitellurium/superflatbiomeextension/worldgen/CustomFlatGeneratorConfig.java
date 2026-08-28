package com.mikitellurium.superflatbiomeextension.worldgen;

import com.google.common.base.Suppliers;
import com.mikitellurium.superflatbiomeextension.registry.ModSurfaceRuleProviders;
import com.mikitellurium.superflatbiomeextension.registry.ModTags;
import com.mikitellurium.superflatbiomeextension.registry.NoiseSettingsRegistry;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.SurfaceRuleProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CustomFlatGeneratorConfig {
    public static final Codec<EnabledFeatures> ENABLED_FEATURES_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("vegetation_features").forGetter(m -> m.isEnabled(ModTags.VEGETATION_FEATURES)),
                    Codec.BOOL.fieldOf("terrain_features").forGetter(m -> m.isEnabled(ModTags.TERRAIN_FEATURES)),
                    Codec.BOOL.fieldOf("lake_features").forGetter(m -> m.isEnabled(ModTags.LAKE_FEATURES)),
                    Codec.BOOL.fieldOf("ore_features").forGetter(m -> m.isEnabled(ModTags.ORE_FEATURES)),
                    Codec.BOOL.fieldOf("misc_features").forGetter(m -> m.isEnabled(ModTags.MISC_FEATURES))
            ).apply(instance, EnabledFeatures::new));
    public static final Codec<CustomFlatGeneratorConfig> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    DimensionSettings.CODEC.fieldOf("dimension_type").forGetter((config -> config.dimensionSettings)),
                    FlatLayer.CODEC.listOf().optionalFieldOf("layers", createDefaultLayers()).forGetter((config) -> config.layers),
                    Codec.BOOL.fieldOf("has_surface_fluid").forGetter((config) -> config.hasSurfaceFluid),
                    ENABLED_FEATURES_CODEC.fieldOf("enabled_features").forGetter((config) -> config.enabledFeatures),
                    RegistryOps.retrieveGetter(Registries.BIOME),
                    RegistryOps.retrieveGetter(Registries.DENSITY_FUNCTION),
                    RegistryOps.retrieveGetter(Registries.NOISE)
            ).apply(instance, CustomFlatGeneratorConfig::new));

    private final DimensionSettings dimensionSettings;
    private final List<FlatLayer> layers;
    private final boolean hasSurfaceFluid;
    private final EnabledFeatures enabledFeatures;
    private final HolderGetter<Biome> biomes;
    private final HolderGetter<DensityFunction> densityFunctions;
    private final HolderGetter<NormalNoise.NoiseParameters> noises;
    private final Supplier<NoiseGeneratorSettings> settings;
    private boolean reduceUndergroundBiomes;

    public CustomFlatGeneratorConfig(DimensionSettings dimensionSettings, List<FlatLayer> layers, boolean surfaceFluid,
                                     EnabledFeatures enabledFeatures, HolderGetter<Biome> biomes,
                                     HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noises) {
        this.dimensionSettings = dimensionSettings;
        this.layers = layers;
        validateLayerCount(this.getLayerAmount());
        this.hasSurfaceFluid = surfaceFluid;
        this.enabledFeatures = enabledFeatures;
        this.biomes = biomes;
        this.densityFunctions = densityFunctions;
        this.noises = noises;
        this.settings = Suppliers.memoize(this::createNoiseSettings);
    }

    public NoiseGeneratorSettings getNoiseGeneratorSettings() {
        return settings.get();
    }

    public BiomeGenerationSettings createBiomeSettings(Holder<Biome> biomeEntry) {
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        BiomeGenerationSettings biomeGenerationSettings = biomeEntry.value().getGenerationSettings();
        List<HolderSet<PlacedFeature>> list = biomeGenerationSettings.features();

        int structuresStep = GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal();
        int strongholdsStep = GenerationStep.Decoration.STRONGHOLDS.ordinal();
        for (int stepIndex = 0; stepIndex < list.size(); stepIndex++) {
            if (stepIndex == structuresStep || stepIndex == strongholdsStep) continue;
            for (Holder<PlacedFeature> feature : list.get(stepIndex)) {
                if (isFeatureAllowed(feature.value().feature())) {
                    builder.addFeature(stepIndex, feature);
                }
            }
        }
        return builder.build();
    }

    private boolean isFeatureAllowed(Holder<ConfiguredFeature<?, ?>> feature) {
        if (feature.is(ModTags.ALWAYS_GENERATE_FEATURES)) {
            return true;
        } else if (feature.is(ModTags.NEVER_GENERATE_FEATURES)) {
            return false;
        }

        for (TagKey<ConfiguredFeature<?, ?>> tag: enabledFeatures.enabledTags()) {
            if (feature.is(tag)) return true;
        }

        return false;
    }

    // Create settings dynamically
    private NoiseGeneratorSettings createNoiseSettings() {
        int surfaceY = dimensionSettings.noiseSettings().minY() + getLayerAmount();
        return new NoiseGeneratorSettings(
                dimensionSettings.noiseSettings(),
                dimensionSettings.defaultBlock(),
                dimensionSettings.defaultFluid(),
                createSurfaceNoiseRouter(this.densityFunctions, this.noises),
                dimensionSettings.surfaceRule().value().apply(this.biomes, surfaceY, this.hasSurfaceFluid()),
                FLAT_SPAWN_TARGET,
                surfaceY - 1,
                false,
                false,
                true,
                false
        );
    }

    private static NoiseRouter createSurfaceNoiseRouter(HolderGetter<DensityFunction> densityFunctionLookup, HolderGetter<NormalNoise.NoiseParameters> noiseParametersLookup) {
        DensityFunction shiftX = densityFunctionLookup.getOrThrow(NoiseRouterData.SHIFT_X).value();
        DensityFunction shiftZ = densityFunctionLookup.getOrThrow(NoiseRouterData.SHIFT_Z).value();
        return new NoiseRouter(
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noiseParametersLookup.getOrThrow(Noises.TEMPERATURE)),
                DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noiseParametersLookup.getOrThrow(Noises.VEGETATION)),
                densityFunctionLookup.getOrThrow(NoiseRouterData.CONTINENTS).value(),
                densityFunctionLookup.getOrThrow(NoiseRouterData.EROSION).value(),
                densityFunctionLookup.getOrThrow(NoiseRouterData.DEPTH).value(),
                densityFunctionLookup.getOrThrow(NoiseRouterData.RIDGES).value(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero()
        );
    }

    private static final List<Climate.ParameterPoint> FLAT_SPAWN_TARGET = createSpawnTarget();

    private static List<Climate.ParameterPoint> createSpawnTarget() {
        Climate.Parameter defaultParameter = Climate.Parameter.span(-1.0F, 1.0F);
        Climate.Parameter riverContinentalness = Climate.Parameter.span(-0.11F, 0.55F);
        Climate.Parameter depth = Climate.Parameter.point(0.0F);
        return List.of(
                new Climate.ParameterPoint(
                        defaultParameter,
                        defaultParameter,
                        Climate.Parameter.span(riverContinentalness, defaultParameter),
                        defaultParameter,
                        depth,
                        Climate.Parameter.span(-1.0F, -0.16F),
                        0L
                ),
                new Climate.ParameterPoint(
                        defaultParameter,
                        defaultParameter,
                        Climate.Parameter.span(riverContinentalness, defaultParameter),
                        defaultParameter,
                        depth,
                        Climate.Parameter.span(0.16F, 1.0F),
                        0L
                )
        );
    }

    public DimensionSettings getDimensionSettings() {
        return this.dimensionSettings;
    }

    public List<FlatLayer> getLayers() {
        return this.layers;
    }

    public int getLayerAmount() {
        return this.layers.stream().mapToInt(FlatLayer::height).sum();
    }

    public HolderGetter<Biome> getBiomes() {
        return this.biomes;
    }

    public HolderGetter<DensityFunction> getDensityFunctions() {
        return this.densityFunctions;
    }

    public HolderGetter<NormalNoise.NoiseParameters> getNoises() {
        return this.noises;
    }

    public boolean hasSurfaceFluid() {
        return this.hasSurfaceFluid;
    }

    public EnabledFeatures getEnabledFeatures() {
        return enabledFeatures;
    }

    public boolean reduceUndergroundBiomes() {
        return reduceUndergroundBiomes;
    }

    public void setReduceUndergroundBiomes(boolean reduceUndergroundBiomes) {
        this.reduceUndergroundBiomes = reduceUndergroundBiomes;
    }

    private static void validateLayerCount(int layerCount) {
        if (layerCount < 1 || layerCount > 384) {
            throw new IllegalArgumentException("Layer count must be between 1 and 384. Got: " + layerCount);
        }
    }

    public static CustomFlatGeneratorConfig createDefault(HolderGetter.Provider holderLookup) {

        return new CustomFlatGeneratorConfig(
                DimensionSettings.getSettings(BuiltinDimensionTypes.OVERWORLD),
                createDefaultLayers(),
                true,
                new EnabledFeatures(true, true, false, true, true),
                holderLookup.lookupOrThrow(Registries.BIOME),
                holderLookup.lookupOrThrow(Registries.DENSITY_FUNCTION),
                holderLookup.lookupOrThrow(Registries.NOISE));
    }

    private static List<FlatLayer> createDefaultLayers() {
        return List.of(
                new FlatLayer(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.BEDROCK), 1),
                new FlatLayer(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.DEEPSLATE), 31),
                new FlatLayer(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.STONE), 32)
        );
    }

    public record FlatLayer(Holder<Block> block, int height) {
        public static final Codec<FlatLayer> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                        BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(FlatLayer::block),
                        Codec.intRange(1, DimensionType.Y_SIZE).fieldOf("height").forGetter(FlatLayer::height)
                ).apply(instance, FlatLayer::new)
        );

        public BlockState blockState() {
            return this.block.value().defaultBlockState();
        }
    }

    public static class EnabledFeatures {
        private final Map<TagKey<ConfiguredFeature<?, ?>>, Boolean> map;
        private final List<TagKey<ConfiguredFeature<?, ?>>> enabledTags;

        public EnabledFeatures(boolean vegetation, boolean terrain, boolean lake, boolean ore, boolean misc) {
            Map<TagKey<ConfiguredFeature<?, ?>>, Boolean> m = new HashMap<>();
            m.put(ModTags.VEGETATION_FEATURES, vegetation);
            m.put(ModTags.TERRAIN_FEATURES, terrain);
            m.put(ModTags.LAKE_FEATURES, lake);
            m.put(ModTags.ORE_FEATURES, ore);
            m.put(ModTags.MISC_FEATURES, misc);
            this.map = Map.copyOf(m);
            this.enabledTags = map.keySet().stream().filter(map::get).toList();
        }

        public boolean isEnabled(TagKey<ConfiguredFeature<?, ?>> tag) {
            return map.getOrDefault(tag, false);
        }

        public List<TagKey<ConfiguredFeature<?, ?>>> enabledTags() {
            return enabledTags;
        }
    }
}

