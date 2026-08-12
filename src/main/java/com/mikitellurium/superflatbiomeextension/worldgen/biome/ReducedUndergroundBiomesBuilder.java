package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

import java.util.List;
import java.util.function.Consumer;

public final class ReducedUndergroundBiomesBuilder {
    private static final float VALLEY_SIZE = 0.05F;
    private static final float LOW_START = 0.26666668F;
    public static final float HIGH_START = 0.4F;
    private static final float HIGH_END = 0.93333334F;
    private static final float PEAK_SIZE = 0.1F;
    public static final float PEAK_START = 0.56666666F;
    private static final float PEAK_END = 0.7666667F;
    public static final float NEAR_INLAND_START = -0.11F;
    public static final float MID_INLAND_START = 0.03F;
    public static final float FAR_INLAND_START = 0.3F;
    public static final float EROSION_INDEX_1_START = -0.78F;
    public static final float EROSION_INDEX_2_START = -0.375F;
    private static final float EROSION_DEEP_DARK_DRYNESS_THRESHOLD = -0.225F;
    private static final float DEPTH_DEEP_DARK_DRYNESS_THRESHOLD = 0.9F;
    private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
    private final Climate.Parameter[] temperatures = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.45F),
            Climate.Parameter.span(-0.45F, -0.15F),
            Climate.Parameter.span(-0.15F, 0.2F),
            Climate.Parameter.span(0.2F, 0.55F),
            Climate.Parameter.span(0.55F, 1.0F)
    };
    private final Climate.Parameter[] humidities = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.35F),
            Climate.Parameter.span(-0.35F, -0.1F),
            Climate.Parameter.span(-0.1F, 0.1F),
            Climate.Parameter.span(0.1F, 0.3F),
            Climate.Parameter.span(0.3F, 1.0F)
    };
    private final Climate.Parameter[] erosions = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.78F),
            Climate.Parameter.span(-0.78F, -0.375F),
            Climate.Parameter.span(-0.375F, -0.2225F),
            Climate.Parameter.span(-0.2225F, 0.05F),
            Climate.Parameter.span(0.05F, 0.45F),
            Climate.Parameter.span(0.45F, 0.55F),
            Climate.Parameter.span(0.55F, 1.0F)
    };
    private final Climate.Parameter FROZEN_RANGE = temperatures[0];
    private final Climate.Parameter UNFROZEN_RANGE = Climate.Parameter.span(temperatures[1], temperatures[4]);
    private final Climate.Parameter mushroomFieldsContinentalness = Climate.Parameter.span(-1.2F, -1.05F);
    private final Climate.Parameter deepOceanContinentalness = Climate.Parameter.span(-1.05F, -0.455F);
    private final Climate.Parameter oceanContinentalness = Climate.Parameter.span(-0.455F, -0.19F);
    private final Climate.Parameter coastContinentalness = Climate.Parameter.span(-0.19F, -0.11F);
    private final Climate.Parameter inlandContinentalness = Climate.Parameter.span(-0.11F, 0.55F);
    private final Climate.Parameter nearInlandContinentalness = Climate.Parameter.span(-0.11F, 0.03F);
    private final Climate.Parameter midInlandContinentalness = Climate.Parameter.span(0.03F, 0.3F);
    private final Climate.Parameter farInlandContinentalness = Climate.Parameter.span(0.3F, 1.0F);
    private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{
            {Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN},
            {Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}
    };
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{
            {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA},
            {Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
            {Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST},
            {Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE},
            {Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT}
    };
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{
            {Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null},
            {null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA},
            {Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null},
            {null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE},
            {null, null, null, null, null}
    };
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{
            {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA},
            {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
            {Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.PALE_GARDEN},
            {Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
            {Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS}
    };
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{
            {Biomes.ICE_SPIKES, null, null, null, null},
            {Biomes.CHERRY_GROVE, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA},
            {Biomes.CHERRY_GROVE, Biomes.CHERRY_GROVE, Biomes.FOREST, Biomes.BIRCH_FOREST, null},
            {null, null, null, null, null},
            {Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}
    };
    private final ResourceKey<Biome>[][] SHATTERED_BIOMES = new ResourceKey[][]{
            {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
            {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
            {Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
            {null, null, null, null, null},
            {null, null, null, null, null}
    };

    public List<Climate.ParameterPoint> spawnTarget() {
        Climate.Parameter surfaceDepth = Climate.Parameter.point(0.0F);
        float riverClearance = 0.16F;
        return List.of(
                new Climate.ParameterPoint(
                        FULL_RANGE,
                        FULL_RANGE,
                        Climate.Parameter.span(inlandContinentalness, FULL_RANGE),
                        FULL_RANGE,
                        surfaceDepth,
                        Climate.Parameter.span(-1.0F, -0.16F),
                        0L
                ),
                new Climate.ParameterPoint(
                        FULL_RANGE,
                        FULL_RANGE,
                        Climate.Parameter.span(inlandContinentalness, FULL_RANGE),
                        FULL_RANGE,
                        surfaceDepth,
                        Climate.Parameter.span(0.16F, 1.0F),
                        0L
                )
        );
    }

    void addBiomes(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
            addDebugBiomes(biomes);
        } else {
            addOffCoastBiomes(biomes);
            addInlandBiomes(biomes);
            addUndergroundBiomes(biomes);
        }
    }

    private void addDebugBiomes(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        HolderLookup.Provider builtIns = new RegistrySetBuilder()
                .add(Registries.DENSITY_FUNCTION, NoiseRouterData::bootstrap)
                .add(Registries.NOISE, NoiseData::bootstrap)
                .build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        HolderGetter<DensityFunction> densityFunctions = builtIns.lookupOrThrow(Registries.DENSITY_FUNCTION);
        DensityFunctions.Spline.Coordinate continents = new DensityFunctions.Spline.Coordinate(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(NoiseRouterData.CONTINENTS))
        );
        DensityFunctions.Spline.Coordinate erosion = new DensityFunctions.Spline.Coordinate(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(NoiseRouterData.EROSION))
        );
        DensityFunctions.Spline.Coordinate ridges = new DensityFunctions.Spline.Coordinate(
                new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(NoiseRouterData.RIDGES_FOLDED))
        );
        biomes.accept(
                Pair.of(
                        Climate.parameters(FULL_RANGE, FULL_RANGE, FULL_RANGE, FULL_RANGE, Climate.Parameter.point(0.0F), FULL_RANGE, 0.01F),
                        Biomes.PLAINS
                )
        );
        if (TerrainProvider.buildErosionOffsetSpline(erosion, ridges, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, Float2FloatFunction.identity()) instanceof CubicSpline.Multipoint<?> multipoint
        )
        {
            ResourceKey<Biome> biome = Biomes.DESERT;

            for (float location : multipoint.locations()) {
                biomes.accept(
                        Pair.of(
                                Climate.parameters(
                                        FULL_RANGE, FULL_RANGE, FULL_RANGE, Climate.Parameter.point(location), Climate.Parameter.point(0.0F), FULL_RANGE, 0.0F
                                ),
                                biome
                        )
                );
                biome = biome == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
            }
        }

        if (TerrainProvider.overworldOffset(continents, erosion, ridges, false) instanceof CubicSpline.Multipoint<?> multipoint) {
            for (float location : multipoint.locations()) {
                biomes.accept(
                        Pair.of(
                                Climate.parameters(
                                        FULL_RANGE, FULL_RANGE, Climate.Parameter.point(location), FULL_RANGE, Climate.Parameter.point(0.0F), FULL_RANGE, 0.0F
                                ),
                                Biomes.SNOWY_TAIGA
                        )
                );
            }
        }
    }

    private void addOffCoastBiomes(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        addSurfaceBiome(
                biomes, FULL_RANGE, FULL_RANGE, mushroomFieldsContinentalness, FULL_RANGE, FULL_RANGE, 0.0F, Biomes.MUSHROOM_FIELDS
        );

        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];
            addSurfaceBiome(
                    biomes, temperature, FULL_RANGE, deepOceanContinentalness, FULL_RANGE, FULL_RANGE, 0.0F, OCEANS[0][temperatureIndex]
            );
            addSurfaceBiome(
                    biomes, temperature, FULL_RANGE, oceanContinentalness, FULL_RANGE, FULL_RANGE, 0.0F, OCEANS[1][temperatureIndex]
            );
        }
    }

    private void addInlandBiomes(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        addMidSlice(biomes, Climate.Parameter.span(-1.0F, -0.93333334F));
        addHighSlice(biomes, Climate.Parameter.span(-0.93333334F, -0.7666667F));
        addPeaks(biomes, Climate.Parameter.span(-0.7666667F, -0.56666666F));
        addHighSlice(biomes, Climate.Parameter.span(-0.56666666F, -0.4F));
        addMidSlice(biomes, Climate.Parameter.span(-0.4F, -0.26666668F));
        addLowSlice(biomes, Climate.Parameter.span(-0.26666668F, -0.05F));
        addValleys(biomes, Climate.Parameter.span(-0.05F, 0.05F));
        addLowSlice(biomes, Climate.Parameter.span(0.05F, 0.26666668F));
        addMidSlice(biomes, Climate.Parameter.span(0.26666668F, 0.4F));
        addHighSlice(biomes, Climate.Parameter.span(0.4F, 0.56666666F));
        addPeaks(biomes, Climate.Parameter.span(0.56666666F, 0.7666667F));
        addHighSlice(biomes, Climate.Parameter.span(0.7666667F, 0.93333334F));
        addMidSlice(biomes, Climate.Parameter.span(0.93333334F, 1.0F));
    }

    private void addPeaks(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, final Climate.Parameter weirdness) {
        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];

            for (int humidityIndex = 0; humidityIndex < humidities.length; humidityIndex++) {
                Climate.Parameter humidity = humidities[humidityIndex];
                ResourceKey<Biome> middleBiome = pickMiddleBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHot = pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHotOrSlopeIfCold = pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> plateauBiome = pickPlateauBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> shatteredBiome = pickShatteredBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> shatteredBiomeOrWindsweptSavanna = maybePickWindsweptSavannaBiome(temperatureIndex, humidityIndex, weirdness, shatteredBiome);
                ResourceKey<Biome> peakBiome = pickPeakBiome(temperatureIndex, humidityIndex, weirdness);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                        erosions[0],
                        weirdness,
                        0.0F,
                        peakBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        erosions[1],
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHotOrSlopeIfCold
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[1],
                        weirdness,
                        0.0F,
                        peakBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        Climate.Parameter.span(erosions[2], erosions[3]),
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[2],
                        weirdness,
                        0.0F,
                        plateauBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, midInlandContinentalness, erosions[3], weirdness, 0.0F, middleBiomeOrBadlandsIfHot);
                addSurfaceBiome(biomes, temperature, humidity, farInlandContinentalness, erosions[3], weirdness, 0.0F, plateauBiome);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                        erosions[4],
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        shatteredBiomeOrWindsweptSavanna
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        shatteredBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                        erosions[6],
                        weirdness,
                        0.0F,
                        middleBiome
                );
            }
        }
    }

    private void addHighSlice(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, final Climate.Parameter weirdness) {
        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];

            for (int humidityIndex = 0; humidityIndex < humidities.length; humidityIndex++) {
                Climate.Parameter humidity = humidities[humidityIndex];
                ResourceKey<Biome> middleBiome = pickMiddleBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHot = pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHotOrSlopeIfCold = pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> plateauBiome = pickPlateauBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> shatteredBiome = pickShatteredBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrWindsweptSavanna = maybePickWindsweptSavannaBiome(temperatureIndex, humidityIndex, weirdness, middleBiome);
                ResourceKey<Biome> slopeBiome = pickSlopeBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> peakBiome = pickPeakBiome(temperatureIndex, humidityIndex, weirdness);
                addSurfaceBiome(
                        biomes, temperature, humidity, coastContinentalness, Climate.Parameter.span(erosions[0], erosions[1]), weirdness, 0.0F, middleBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, nearInlandContinentalness, erosions[0], weirdness, 0.0F, slopeBiome);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[0],
                        weirdness,
                        0.0F,
                        peakBiome
                );
                addSurfaceBiome(
                        biomes, temperature, humidity, nearInlandContinentalness, erosions[1], weirdness, 0.0F, middleBiomeOrBadlandsIfHotOrSlopeIfCold
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[1],
                        weirdness,
                        0.0F,
                        slopeBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        Climate.Parameter.span(erosions[2], erosions[3]),
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[2],
                        weirdness,
                        0.0F,
                        plateauBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, midInlandContinentalness, erosions[3], weirdness, 0.0F, middleBiomeOrBadlandsIfHot);
                addSurfaceBiome(biomes, temperature, humidity, farInlandContinentalness, erosions[3], weirdness, 0.0F, plateauBiome);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                        erosions[4],
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        middleBiomeOrWindsweptSavanna
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        shatteredBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                        erosions[6],
                        weirdness,
                        0.0F,
                        middleBiome
                );
            }
        }
    }

    private void addMidSlice(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, final Climate.Parameter weirdness) {
        addSurfaceBiome(
                biomes,
                FULL_RANGE,
                FULL_RANGE,
                coastContinentalness,
                Climate.Parameter.span(erosions[0], erosions[2]),
                weirdness,
                0.0F,
                Biomes.STONY_SHORE
        );
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[1], temperatures[2]),
                FULL_RANGE,
                Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.SWAMP
        );
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[3], temperatures[4]),
                FULL_RANGE,
                Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.MANGROVE_SWAMP
        );

        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];

            for (int humidityIndex = 0; humidityIndex < humidities.length; humidityIndex++) {
                Climate.Parameter humidity = humidities[humidityIndex];
                ResourceKey<Biome> middleBiome = pickMiddleBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHot = pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHotOrSlopeIfCold = pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> shatteredBiome = pickShatteredBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> plateauBiome = pickPlateauBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> beachBiome = pickBeachBiome(temperatureIndex, humidityIndex);
                ResourceKey<Biome> middleBiomeOrWindsweptSavanna = maybePickWindsweptSavannaBiome(temperatureIndex, humidityIndex, weirdness, middleBiome);
                ResourceKey<Biome> shatteredCoastBiome = pickShatteredCoastBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> slopeBiome = pickSlopeBiome(temperatureIndex, humidityIndex, weirdness);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                        erosions[0],
                        weirdness,
                        0.0F,
                        slopeBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(nearInlandContinentalness, midInlandContinentalness),
                        erosions[1],
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHotOrSlopeIfCold
                );
                addSurfaceBiome(
                        biomes, temperature, humidity, farInlandContinentalness, erosions[1], weirdness, 0.0F, temperatureIndex == 0 ? slopeBiome : plateauBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, nearInlandContinentalness, erosions[2], weirdness, 0.0F, middleBiome);
                addSurfaceBiome(biomes, temperature, humidity, midInlandContinentalness, erosions[2], weirdness, 0.0F, middleBiomeOrBadlandsIfHot);
                addSurfaceBiome(biomes, temperature, humidity, farInlandContinentalness, erosions[2], weirdness, 0.0F, plateauBiome);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(coastContinentalness, nearInlandContinentalness),
                        erosions[3],
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[3],
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHot
                );
                if (weirdness.max() < 0L) {
                    addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[4], weirdness, 0.0F, beachBiome);
                    addSurfaceBiome(
                            biomes,
                            temperature,
                            humidity,
                            Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                            erosions[4],
                            weirdness,
                            0.0F,
                            middleBiome
                    );
                } else {
                    addSurfaceBiome(
                            biomes,
                            temperature,
                            humidity,
                            Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                            erosions[4],
                            weirdness,
                            0.0F,
                            middleBiome
                    );
                }

                addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[5], weirdness, 0.0F, shatteredCoastBiome);
                addSurfaceBiome(biomes, temperature, humidity, nearInlandContinentalness, erosions[5], weirdness, 0.0F, middleBiomeOrWindsweptSavanna);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        shatteredBiome
                );
                if (weirdness.max() < 0L) {
                    addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[6], weirdness, 0.0F, beachBiome);
                } else {
                    addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[6], weirdness, 0.0F, middleBiome);
                }

                if (temperatureIndex == 0) {
                    addSurfaceBiome(
                            biomes,
                            temperature,
                            humidity,
                            Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                            erosions[6],
                            weirdness,
                            0.0F,
                            middleBiome
                    );
                }
            }
        }
    }

    private void addLowSlice(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, final Climate.Parameter weirdness) {
        addSurfaceBiome(
                biomes,
                FULL_RANGE,
                FULL_RANGE,
                coastContinentalness,
                Climate.Parameter.span(erosions[0], erosions[2]),
                weirdness,
                0.0F,
                Biomes.STONY_SHORE
        );
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[1], temperatures[2]),
                FULL_RANGE,
                Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.SWAMP
        );
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[3], temperatures[4]),
                FULL_RANGE,
                Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.MANGROVE_SWAMP
        );

        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];

            for (int humidityIndex = 0; humidityIndex < humidities.length; humidityIndex++) {
                Climate.Parameter humidity = humidities[humidityIndex];
                ResourceKey<Biome> middleBiome = pickMiddleBiome(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHot = pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> middleBiomeOrBadlandsIfHotOrSlopeIfCold = pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(temperatureIndex, humidityIndex, weirdness);
                ResourceKey<Biome> beachBiome = pickBeachBiome(temperatureIndex, humidityIndex);
                ResourceKey<Biome> middleBiomeOrWindsweptSavanna = maybePickWindsweptSavannaBiome(temperatureIndex, humidityIndex, weirdness, middleBiome);
                ResourceKey<Biome> shatteredCoastBiome = pickShatteredCoastBiome(temperatureIndex, humidityIndex, weirdness);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        nearInlandContinentalness,
                        Climate.Parameter.span(erosions[0], erosions[1]),
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHot
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        Climate.Parameter.span(erosions[0], erosions[1]),
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHotOrSlopeIfCold
                );
                addSurfaceBiome(
                        biomes, temperature, humidity, nearInlandContinentalness, Climate.Parameter.span(erosions[2], erosions[3]), weirdness, 0.0F, middleBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        Climate.Parameter.span(erosions[2], erosions[3]),
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHot
                );
                addSurfaceBiome(
                        biomes, temperature, humidity, coastContinentalness, Climate.Parameter.span(erosions[3], erosions[4]), weirdness, 0.0F, beachBiome
                );
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                        erosions[4],
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[5], weirdness, 0.0F, shatteredCoastBiome);
                addSurfaceBiome(biomes, temperature, humidity, nearInlandContinentalness, erosions[5], weirdness, 0.0F, middleBiomeOrWindsweptSavanna);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        erosions[5],
                        weirdness,
                        0.0F,
                        middleBiome
                );
                addSurfaceBiome(biomes, temperature, humidity, coastContinentalness, erosions[6], weirdness, 0.0F, beachBiome);
                if (temperatureIndex == 0) {
                    addSurfaceBiome(
                            biomes,
                            temperature,
                            humidity,
                            Climate.Parameter.span(nearInlandContinentalness, farInlandContinentalness),
                            erosions[6],
                            weirdness,
                            0.0F,
                            middleBiome
                    );
                }
            }
        }
    }

    private void addValleys(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, final Climate.Parameter weirdness) {
        addSurfaceBiome(
                biomes,
                FROZEN_RANGE,
                FULL_RANGE,
                coastContinentalness,
                Climate.Parameter.span(erosions[0], erosions[1]),
                weirdness,
                0.0F,
                weirdness.max() < 0L ? Biomes.STONY_SHORE : Biomes.FROZEN_RIVER
        );
        addSurfaceBiome(
                biomes,
                UNFROZEN_RANGE,
                FULL_RANGE,
                coastContinentalness,
                Climate.Parameter.span(erosions[0], erosions[1]),
                weirdness,
                0.0F,
                weirdness.max() < 0L ? Biomes.STONY_SHORE : Biomes.RIVER
        );
        addSurfaceBiome(
                biomes,
                FROZEN_RANGE,
                FULL_RANGE,
                nearInlandContinentalness,
                Climate.Parameter.span(erosions[0], erosions[1]),
                weirdness,
                0.0F,
                Biomes.FROZEN_RIVER
        );
        addSurfaceBiome(
                biomes,
                UNFROZEN_RANGE,
                FULL_RANGE,
                nearInlandContinentalness,
                Climate.Parameter.span(erosions[0], erosions[1]),
                weirdness,
                0.0F,
                Biomes.RIVER
        );
        addSurfaceBiome(
                biomes,
                FROZEN_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                Climate.Parameter.span(erosions[2], erosions[5]),
                weirdness,
                0.0F,
                Biomes.FROZEN_RIVER
        );
        addSurfaceBiome(
                biomes,
                UNFROZEN_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(coastContinentalness, farInlandContinentalness),
                Climate.Parameter.span(erosions[2], erosions[5]),
                weirdness,
                0.0F,
                Biomes.RIVER
        );
        addSurfaceBiome(biomes, FROZEN_RANGE, FULL_RANGE, coastContinentalness, erosions[6], weirdness, 0.0F, Biomes.FROZEN_RIVER);
        addSurfaceBiome(biomes, UNFROZEN_RANGE, FULL_RANGE, coastContinentalness, erosions[6], weirdness, 0.0F, Biomes.RIVER);
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[1], temperatures[2]),
                FULL_RANGE,
                Climate.Parameter.span(inlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.SWAMP
        );
        addSurfaceBiome(
                biomes,
                Climate.Parameter.span(temperatures[3], temperatures[4]),
                FULL_RANGE,
                Climate.Parameter.span(inlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.MANGROVE_SWAMP
        );
        addSurfaceBiome(
                biomes,
                FROZEN_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(inlandContinentalness, farInlandContinentalness),
                erosions[6],
                weirdness,
                0.0F,
                Biomes.FROZEN_RIVER
        );

        for (int temperatureIndex = 0; temperatureIndex < temperatures.length; temperatureIndex++) {
            Climate.Parameter temperature = temperatures[temperatureIndex];

            for (int humidityIndex = 0; humidityIndex < humidities.length; humidityIndex++) {
                Climate.Parameter humidity = humidities[humidityIndex];
                ResourceKey<Biome> middleBiomeOrBadlandsIfHot = pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
                addSurfaceBiome(
                        biomes,
                        temperature,
                        humidity,
                        Climate.Parameter.span(midInlandContinentalness, farInlandContinentalness),
                        Climate.Parameter.span(erosions[0], erosions[1]),
                        weirdness,
                        0.0F,
                        middleBiomeOrBadlandsIfHot
                );
            }
        }
    }

    /*
     * Changes:
     * Dripstone Caves depth: (0.2F, 0.9F) -> 1.0F
     * Lush Caves depth: (0.2, 0.9F) -> 1.0F
     * Sulfur Caves depth: (0.2F, 0.9F) -> (0.7F, 1.4F)
     */
    private void addUndergroundBiomes(final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        addUndergroundBiome(
                biomes,
                FULL_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(0.8F, 1.0F),
                FULL_RANGE,
                Climate.Parameter.point(1.0F),
                FULL_RANGE,
                0.0F,
                Biomes.DRIPSTONE_CAVES
        );
        addUndergroundBiome(
                biomes,
                FULL_RANGE,
                Climate.Parameter.span(0.7F, 1.0F),
                FULL_RANGE,
                FULL_RANGE,
                Climate.Parameter.point(1.0F),
                FULL_RANGE,
                0.0F,
                Biomes.LUSH_CAVES
        );
        addUndergroundBiome(
                biomes,
                FULL_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(coastContinentalness, inlandContinentalness),
                Climate.Parameter.span(erosions[5], erosions[6]),
                Climate.Parameter.span(0.7F, 1.4F),
                Climate.Parameter.span(-1.1F, -0.85F),
                0.0F,
                Biomes.SULFUR_CAVES
        );
        addBottomBiome(
                biomes,
                FULL_RANGE,
                FULL_RANGE,
                FULL_RANGE,
                Climate.Parameter.span(erosions[0], erosions[1]),
                FULL_RANGE,
                0.0F,
                Biomes.DEEP_DARK
        );
    }

    private ResourceKey<Biome> pickMiddleBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        if (weirdness.max() < 0L) {
            return MIDDLE_BIOMES[temperatureIndex][humidityIndex];
        }

        ResourceKey<Biome> variant = MIDDLE_BIOMES_VARIANT[temperatureIndex][humidityIndex];
        return variant == null ? MIDDLE_BIOMES[temperatureIndex][humidityIndex] : variant;
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        return temperatureIndex == 4 ? pickBadlandsBiome(humidityIndex, weirdness) : pickMiddleBiome(temperatureIndex, humidityIndex, weirdness);
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        return temperatureIndex == 0
                ? pickSlopeBiome(temperatureIndex, humidityIndex, weirdness)
                : pickMiddleBiomeOrBadlandsIfHot(temperatureIndex, humidityIndex, weirdness);
    }

    private ResourceKey<Biome> maybePickWindsweptSavannaBiome(
            final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness, final ResourceKey<Biome> underlyingBiome
    ) {
        return temperatureIndex > 1 && humidityIndex < 4 && weirdness.max() >= 0L ? Biomes.WINDSWEPT_SAVANNA : underlyingBiome;
    }

    private ResourceKey<Biome> pickShatteredCoastBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        ResourceKey<Biome> beachOrMiddleBiome = weirdness.max() >= 0L
                ? pickMiddleBiome(temperatureIndex, humidityIndex, weirdness)
                : pickBeachBiome(temperatureIndex, humidityIndex);
        return maybePickWindsweptSavannaBiome(temperatureIndex, humidityIndex, weirdness, beachOrMiddleBiome);
    }

    private ResourceKey<Biome> pickBeachBiome(final int temperatureIndex, final int humidityIndex) {
        if (temperatureIndex == 0) {
            return Biomes.SNOWY_BEACH;
        } else {
            return temperatureIndex == 4 ? Biomes.DESERT : Biomes.BEACH;
        }
    }

    private ResourceKey<Biome> pickBadlandsBiome(final int humidityIndex, final Climate.Parameter weirdness) {
        if (humidityIndex < 2) {
            return weirdness.max() < 0L ? Biomes.BADLANDS : Biomes.ERODED_BADLANDS;
        } else {
            return humidityIndex < 3 ? Biomes.BADLANDS : Biomes.WOODED_BADLANDS;
        }
    }

    private ResourceKey<Biome> pickPlateauBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        if (weirdness.max() >= 0L) {
            ResourceKey<Biome> variant = PLATEAU_BIOMES_VARIANT[temperatureIndex][humidityIndex];
            if (variant != null) {
                return variant;
            }
        }

        return PLATEAU_BIOMES[temperatureIndex][humidityIndex];
    }

    private ResourceKey<Biome> pickPeakBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        if (temperatureIndex <= 2) {
            return weirdness.max() < 0L ? Biomes.JAGGED_PEAKS : Biomes.FROZEN_PEAKS;
        } else {
            return temperatureIndex == 3 ? Biomes.STONY_PEAKS : pickBadlandsBiome(humidityIndex, weirdness);
        }
    }

    private ResourceKey<Biome> pickSlopeBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        if (temperatureIndex >= 3) {
            return pickPlateauBiome(temperatureIndex, humidityIndex, weirdness);
        } else {
            return humidityIndex <= 1 ? Biomes.SNOWY_SLOPES : Biomes.GROVE;
        }
    }

    private ResourceKey<Biome> pickShatteredBiome(final int temperatureIndex, final int humidityIndex, final Climate.Parameter weirdness) {
        ResourceKey<Biome> biome = SHATTERED_BIOMES[temperatureIndex][humidityIndex];
        return biome == null ? pickMiddleBiome(temperatureIndex, humidityIndex, weirdness) : biome;
    }

    private void addSurfaceBiome(
            final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes,
            final Climate.Parameter temperature,
            final Climate.Parameter humidity,
            final Climate.Parameter continentalness,
            final Climate.Parameter erosion,
            final Climate.Parameter weirdness,
            final float offset,
            final ResourceKey<Biome> second
    ) {
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(0.0F), weirdness, offset), second));
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.0F), weirdness, offset), second));
    }

    // Added depth argument
    private void addUndergroundBiome(
            final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes,
            final Climate.Parameter temperature,
            final Climate.Parameter humidity,
            final Climate.Parameter continentalness,
            final Climate.Parameter erosion,
            final Climate.Parameter depth,
            final Climate.Parameter weirdness,
            final float offset,
            final ResourceKey<Biome> biome
    ) {
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, offset), biome));
    }

    private void addBottomBiome(
            final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes,
            final Climate.Parameter temperature,
            final Climate.Parameter humidity,
            final Climate.Parameter continentalness,
            final Climate.Parameter erosion,
            final Climate.Parameter weirdness,
            final float offset,
            final ResourceKey<Biome> biome
    ) {
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.1F), weirdness, offset), biome));
    }
}
