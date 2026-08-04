package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class ModSurfaceRules {
    private static final SurfaceRules.RuleSource TERRACOTTA = block(Blocks.TERRACOTTA);
    private static final SurfaceRules.RuleSource RED_SAND = block(Blocks.RED_SAND);
    private static final SurfaceRules.RuleSource RED_SANDSTONE = block(Blocks.RED_SANDSTONE);
    private static final SurfaceRules.RuleSource STONE = block(Blocks.STONE);
    private static final SurfaceRules.RuleSource DEEPSLATE = block(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource DIRT = block(Blocks.DIRT);
    private static final SurfaceRules.RuleSource PODZOL = block(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource COARSE_DIRT = block(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource MYCELIUM = block(Blocks.MYCELIUM);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = block(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource GRAVEL = block(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = block(Blocks.SAND);
    private static final SurfaceRules.RuleSource SANDSTONE = block(Blocks.SANDSTONE);
    private static final SurfaceRules.RuleSource PACKED_ICE = block(Blocks.PACKED_ICE);
    private static final SurfaceRules.RuleSource SNOW_BLOCK = block(Blocks.SNOW_BLOCK);
    private static final SurfaceRules.RuleSource MUD = block(Blocks.MUD);
    private static final SurfaceRules.RuleSource POWDER_SNOW = block(Blocks.POWDER_SNOW);
    private static final SurfaceRules.RuleSource MOSS_BLOCK = block(Blocks.MOSS_BLOCK);
    private static final SurfaceRules.RuleSource ICE = block(Blocks.ICE);
    private static final SurfaceRules.RuleSource WATER = block(Blocks.WATER);

    public static SurfaceRules.RuleSource createDefaultModSurfaceRule(HolderGetter<Biome> biomes, int surfaceY, boolean generateWater) {
        SurfaceYGetter mapY = (i) -> surfaceY + i;
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = new ImmutableList.Builder<>();
        SurfaceRules.RuleSource grassRule = SurfaceRules.sequence(
                topToBottomInclusive(mapY.get(-1), mapY.get(-1), GRASS_BLOCK),
                topToBottomInclusive(mapY.get(-2), mapY.get(-3), DIRT)
        );
        SurfaceRules.RuleSource waterRule = SurfaceRules.ifTrue(
                SurfaceRules.waterBlockCheck(mapY.get(0), mapY.get(0)),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-3)), 0), SAND)
        );
        SurfaceRules.RuleSource sandFloorRule = SurfaceRules.ifTrue(
                SurfaceRules.waterBlockCheck(mapY.get(0), mapY.get(0)),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-3)), 0), SAND));
        SurfaceRules.RuleSource gravelFloorRule = SurfaceRules.ifTrue(
                SurfaceRules.waterBlockCheck(mapY.get(0), mapY.get(0)),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-3)), 0), GRAVEL));
        SurfaceRules.ConditionSource materialCondition1 = SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.909, -0.5454);
        SurfaceRules.ConditionSource materialCondition2 = SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.1818, 0.1818);
        SurfaceRules.ConditionSource materialCondition3 = SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.5454, 0.909);
        SurfaceRules.RuleSource powderSnowRule1 = SurfaceRules.ifTrue(
                SurfaceRules.noiseCondition2d(Noises.POWDER_SNOW, 0.45, 0.58), POWDER_SNOW);
        SurfaceRules.RuleSource powderSnowRule2 = SurfaceRules.ifTrue(
                SurfaceRules.noiseCondition2d(Noises.POWDER_SNOW, 0.35, 0.6), POWDER_SNOW);
        SurfaceRules.RuleSource gravelRule = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, STONE), GRAVEL);
        SurfaceRules.RuleSource materialRule = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.DESERT),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), SAND),
                                topToBottomInclusive(mapY.get(-4), mapY.get(-10), SANDSTONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.BADLANDS, Biomes.ERODED_BADLANDS),
                        SurfaceRules.ifTrue(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-12)), 0),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(materialCondition1,
                                                SurfaceRules.sequence(
                                                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), RED_SAND),
                                                        topToBottomInclusive(mapY.get(-4), mapY.get(-10), RED_SANDSTONE)
                                                )),
                                        SurfaceRules.ifTrue(materialCondition1, TERRACOTTA),
                                        SurfaceRules.ifTrue(materialCondition2, TERRACOTTA),
                                        SurfaceRules.ifTrue(materialCondition3, TERRACOTTA),
                                        SurfaceRules.bandlands(), RED_SANDSTONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.WOODED_BADLANDS),
                        SurfaceRules.ifTrue(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-2)), 0),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(materialCondition1, COARSE_DIRT),
                                        SurfaceRules.ifTrue(materialCondition2, COARSE_DIRT),
                                        SurfaceRules.ifTrue(materialCondition3, COARSE_DIRT),
                                        grassRule
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.RIVER, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? WATER : SAND),
                                sandFloorRule
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.FROZEN_RIVER),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? ICE : SAND),
                                sandFloorRule
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? WATER : GRAVEL),
                                gravelFloorRule
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? ICE : GRAVEL),
                                gravelFloorRule
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.BEACH, Biomes.SNOWY_BEACH),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), SAND),
                                topToBottomInclusive(mapY.get(-4), mapY.get(-5), SANDSTONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.STONY_SHORE, Biomes.STONY_PEAKS),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(surfaceNoiseThreshold(1.75), COARSE_DIRT),
                                SurfaceRules.ifTrue(surfaceNoiseThreshold(-0.95), PODZOL))
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.MANGROVE_SWAMP),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, Biomes.MANGROVE_SWAMP),
                                        topToBottomInclusive(mapY.get(-2), mapY.get(-4), MUD)
                                ),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-1)), 0),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition2d(Noises.SWAMP, 0.0), generateWater ? WATER : MUD)
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.SWAMP),
                        SurfaceRules.ifTrue(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-1)), 0),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition2d(Noises.SWAMP, 0.0), generateWater ? WATER : GRASS_BLOCK)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.WINDSWEPT_GRAVELLY_HILLS),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-3),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(surfaceNoiseThreshold(2.0), gravelRule),
                                        SurfaceRules.ifTrue(surfaceNoiseThreshold(1.0), STONE),
                                        SurfaceRules.ifTrue(surfaceNoiseThreshold(-1.0), DIRT),
                                        gravelRule
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.SNOWY_SLOPES),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), powderSnowRule1),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), powderSnowRule2),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.JAGGED_PEAKS),
                        SurfaceRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK),
                                topToBottomInclusive(mapY.get(-2), mapY.get(-3), STONE)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.GROVE),
                        SurfaceRules.ifTrue(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-3)), 0),
                                powderSnowRule1
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.FROZEN_PEAKS),
                        SurfaceRules.ifTrue(
                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-12)), 0),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition2d(Noises.PACKED_ICE, 0.0, 0.2), PACKED_ICE),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition2d(Noises.ICE, 0.0, 0.025), ICE),
                                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                                )
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.ICE_SPIKES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.DRIPSTONE_CAVES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.LUSH_CAVES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), MOSS_BLOCK)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.DEEP_DARK), DEEPSLATE
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.MUSHROOM_FIELDS),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), MYCELIUM)
                ),
                grassRule,
                waterRule
        );
        builder.add(materialRule);
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }


    private static SurfaceRules.RuleSource topToBottomInclusive(int top, int bottom, SurfaceRules.RuleSource rule) {
        return SurfaceRules.ifTrue(
                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(bottom), 0),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(top + 1), 0)), rule));
    }

    private static SurfaceRules.ConditionSource surfaceNoiseThreshold(double min) {
        return SurfaceRules.noiseCondition2d(Noises.SURFACE, min / 8.25, Double.MAX_VALUE);
    }

    private static SurfaceRules.RuleSource block(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    @FunctionalInterface
    interface SurfaceYGetter {
        int get(int i);
    }
}
