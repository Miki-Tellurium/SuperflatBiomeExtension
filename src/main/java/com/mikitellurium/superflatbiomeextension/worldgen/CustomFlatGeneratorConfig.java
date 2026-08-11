package com.mikitellurium.superflatbiomeextension.worldgen;

import com.google.common.base.Suppliers;
import com.mikitellurium.superflatbiomeextension.registry.NoiseSettingsRegistry;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.ModSurfaceRules;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CustomFlatGeneratorConfig {
    public static final MapCodec<CustomFlatGeneratorConfig> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    NoiseSettingsRegistry.CODEC.optionalFieldOf("noise_settings", NoiseSettingsRegistry.SURFACE).forGetter((config) -> config.shapeConfig),
                    Codec.INT.fieldOf("layer_count").forGetter((config) -> config.layerCount),
                    Codec.BOOL.fieldOf("generate_water").forGetter((config) -> config.generateWater),
                    Codec.BOOL.fieldOf("has_features").forGetter((config) -> config.hasFeatures),
                    Codec.BOOL.fieldOf("has_structures").forGetter(CustomFlatGeneratorConfig::hasStructures),
                    Codec.BOOL.fieldOf("has_lava_lakes").forGetter(CustomFlatGeneratorConfig::hasLakes),
                    Codec.BOOL.fieldOf("generate_ores").forGetter(CustomFlatGeneratorConfig::generateOres),
                    FlatLayer.CODEC.listOf().optionalFieldOf("layers", List.of()).forGetter((config) -> config.layers),
                    RegistryOps.retrieveGetter(Registries.BIOME),
                    RegistryOps.retrieveGetter(Registries.DENSITY_FUNCTION),
                    RegistryOps.retrieveGetter(Registries.NOISE)
            ).apply(instance, CustomFlatGeneratorConfig::new)
    );
    private final NoiseSettings shapeConfig;
    private final int layerCount;
    private final boolean generateWater;
    private final boolean hasFeatures;
    private final Map<Integer, FeatureStepCheck> featureChecks;
    private final List<FlatLayer> layers;
    private final HolderGetter<Biome> biomes;
    private final HolderGetter<DensityFunction> densityFunctions;
    private final HolderGetter<NormalNoise.NoiseParameters> noises;
    private final Supplier<NoiseGeneratorSettings> settings;

    public CustomFlatGeneratorConfig(NoiseSettings shapeConfig, int layerCount, boolean generateWater, boolean hasFeatures, boolean hasStructures, boolean hasLakes, boolean generateOres, List<FlatLayer> layers,
                                     HolderGetter<Biome> biomes, HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noises) {
        validateLayerCount(layerCount);
        this.shapeConfig = shapeConfig;
        this.layerCount = layerCount;
        this.generateWater = generateWater;
        this.hasFeatures = hasFeatures;
        this.featureChecks = Map.of(
                GenerationStep.Decoration.LAKES.ordinal(), new FeatureStepCheck(hasLakes, GenerationStep.Decoration.LAKES),
                GenerationStep.Decoration.UNDERGROUND_ORES.ordinal(), new FeatureStepCheck(generateOres, GenerationStep.Decoration.UNDERGROUND_ORES),
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Decoration.UNDERGROUND_STRUCTURES),
                GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Decoration.SURFACE_STRUCTURES),
                GenerationStep.Decoration.STRONGHOLDS.ordinal(), new FeatureStepCheck(hasStructures, GenerationStep.Decoration.STRONGHOLDS)
        );
        this.layers = layers;
        this.biomes = biomes;
        this.densityFunctions = densityFunctions;
        this.noises = noises;
        this.settings = Suppliers.memoize(this::createSettings);
    }

    public record FlatLayer(Holder<Block> block, int height) {
        public static final Codec<FlatLayer> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(FlatLayer::block),
                        Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(FlatLayer::height)
                ).apply(instance, FlatLayer::new)
        );

        public BlockState blockState() {
            return this.block.value().defaultBlockState();
        }
    }

    public NoiseGeneratorSettings getChunkGeneratorSettings() {
        return settings.get();
    }

    public BiomeGenerationSettings createGenerationSettings(Holder<Biome> biomeEntry) {
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        BiomeGenerationSettings biomeGenerationSettings = biomeEntry.value().getGenerationSettings();
        List<HolderSet<PlacedFeature>> list = biomeGenerationSettings.features();
        for (int i = 0; i < list.size(); i++) {
            if ((this.hasFeatures() && !featureChecks.containsKey(i))
                    || (this.featureChecks.containsKey(i) && this.featureChecks.get(i).test(i)))
                for (Holder<PlacedFeature> feature : list.get(i)) {
                    builder.addFeature(i, feature);
                }
        }
        return builder.build();
    }

    // Create settings dynamically
    private NoiseGeneratorSettings createSettings() {
        int surfaceY = shapeConfig.minY() + getTotalHeight();
        return new NoiseGeneratorSettings(
                this.shapeConfig,
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                createSurfaceNoiseRouter(this.densityFunctions, this.noises),
                ModSurfaceRules.createDefaultModSurfaceRule(this.biomes, surfaceY, this.generateWater()),
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

    public NoiseSettings getGenerationShapeConfig() {
        return this.shapeConfig;
    }

    public int getLayerCount() {
        return this.layerCount;
    }

    public List<FlatLayer> getLayers() {
        return this.layers;
    }

    public boolean hasCustomLayers() {
        return !this.layers.isEmpty();
    }

    public List<FlatLayer> getResolvedLayers() {
        if (this.hasCustomLayers()) {
            return this.layers;
        }
        return List.of(
                new FlatLayer(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.BEDROCK), 1),
                new FlatLayer(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.STONE), this.layerCount - 1)
        );
    }

    public int getTotalHeight() {
        if (this.hasCustomLayers()) {
            return this.layers.stream().mapToInt(FlatLayer::height).sum();
        }
        return this.layerCount;
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

    public boolean generateWater() {
        return this.generateWater;
    }

    public boolean hasFeatures() {
        return this.hasFeatures;
    }

    public boolean hasLakes() {
        return featureChecks.get(GenerationStep.Decoration.LAKES.ordinal()).isEnabled();
    }

    public boolean generateOres() {
        return featureChecks.get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal()).isEnabled();
    }

    public boolean hasStructures() {
        return featureChecks.get(GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal()).isEnabled() ||
                featureChecks.get(GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()).isEnabled() ||
                featureChecks.get(GenerationStep.Decoration.STRONGHOLDS.ordinal()).isEnabled();
    }

    private static void validateLayerCount(int layerCount) {
        if (layerCount < 1 || layerCount > 384) {
            throw new IllegalArgumentException("Layer count must be between 1 and 384. Got: " + layerCount);
        }
    }

    public static CustomFlatGeneratorConfig createDefault(HolderGetter.Provider holderLookup) {
        return new CustomFlatGeneratorConfig(
                NoiseSettingsRegistry.SURFACE,
                64,
                true,
                true,
                true,
                false,
                false,
                List.of(),
                holderLookup.lookupOrThrow(Registries.BIOME),
                holderLookup.lookupOrThrow(Registries.DENSITY_FUNCTION),
                holderLookup.lookupOrThrow(Registries.NOISE)
        );
    }

    private record FeatureStepCheck(boolean isEnabled, GenerationStep.Decoration featureStep) implements Predicate<Integer> {
        @Override
        public boolean test(Integer i) {
            return isEnabled && i == featureStep.ordinal();
        }
    }
}

