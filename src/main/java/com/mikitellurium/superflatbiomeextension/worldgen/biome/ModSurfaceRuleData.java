package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.google.common.collect.ImmutableList;
import com.mikitellurium.superflatbiomeextension.registry.ModNoises;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class ModSurfaceRuleData {
    private static final SurfaceRules.RuleSource TERRACOTTA = stateRule(Blocks.TERRACOTTA);
    private static final SurfaceRules.RuleSource RED_SAND = stateRule(Blocks.RED_SAND);
    private static final SurfaceRules.RuleSource RED_SANDSTONE = stateRule(Blocks.RED_SANDSTONE);
    private static final SurfaceRules.RuleSource STONE = stateRule(Blocks.STONE);
    private static final SurfaceRules.RuleSource DEEPSLATE = stateRule(Blocks.DEEPSLATE);
    private static final SurfaceRules.RuleSource DIRT = stateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource PODZOL = stateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource COARSE_DIRT = stateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource MYCELIUM = stateRule(Blocks.MYCELIUM);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = stateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource GRAVEL = stateRule(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = stateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource SANDSTONE = stateRule(Blocks.SANDSTONE);
    private static final SurfaceRules.RuleSource PACKED_ICE = stateRule(Blocks.PACKED_ICE);
    private static final SurfaceRules.RuleSource SNOW_BLOCK = stateRule(Blocks.SNOW_BLOCK);
    private static final SurfaceRules.RuleSource MUD = stateRule(Blocks.MUD);
    private static final SurfaceRules.RuleSource POWDER_SNOW = stateRule(Blocks.POWDER_SNOW);
    private static final SurfaceRules.RuleSource MOSS_BLOCK = stateRule(Blocks.MOSS_BLOCK);
    private static final SurfaceRules.RuleSource ICE = stateRule(Blocks.ICE);
    private static final SurfaceRules.RuleSource WATER = stateRule(Blocks.WATER);
    private static final SurfaceRules.RuleSource CINNABAR = stateRule(Blocks.CINNABAR);
    private static final SurfaceRules.RuleSource SULFUR = stateRule(Blocks.SULFUR);
    private static final SurfaceRules.RuleSource LAVA = stateRule(Blocks.LAVA);
    private static final SurfaceRules.RuleSource NETHERRACK = stateRule(Blocks.NETHERRACK);
    private static final SurfaceRules.RuleSource SOUL_SAND = stateRule(Blocks.SOUL_SAND);
    private static final SurfaceRules.RuleSource SOUL_SOIL = stateRule(Blocks.SOUL_SOIL);
    private static final SurfaceRules.RuleSource BASALT = stateRule(Blocks.BASALT);
    private static final SurfaceRules.RuleSource BLACKSTONE = stateRule(Blocks.BLACKSTONE);
    private static final SurfaceRules.RuleSource WARPED_WART_BLOCK = stateRule(Blocks.WARPED_WART_BLOCK);
    private static final SurfaceRules.RuleSource WARPED_NYLIUM = stateRule(Blocks.WARPED_NYLIUM);
    private static final SurfaceRules.RuleSource NETHER_WART_BLOCK = stateRule(Blocks.NETHER_WART_BLOCK);
    private static final SurfaceRules.RuleSource CRIMSON_NYLIUM = stateRule(Blocks.CRIMSON_NYLIUM);

    public static SurfaceRules.RuleSource overworld(HolderGetter<Biome> biomes, int surfaceY, boolean generateWater) {
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
        SurfaceRules.RuleSource sulfurCaveBands = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition3d(Noises.SULFUR_CAVE_GRADIENT, -0.4F, -0.1F), CINNABAR),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition3d(Noises.SULFUR_CAVE_GRADIENT, 0.0, 0.4F), SULFUR),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition3d(Noises.SULFUR_CAVE_GRADIENT, 0.4F), CINNABAR)
        );
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
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.SULFUR_CAVES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-5),
                            SurfaceRules.sequence(sulfurCaveBands, STONE)
                        )
                ),
                grassRule,
                waterRule
        );
        builder.add(materialRule);
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }

    public static SurfaceRules.RuleSource nether(final HolderGetter<Biome> biomes, int surfaceY) {
        SurfaceYGetter mapY = (i) -> surfaceY + i;
        SurfaceRules.ConditionSource closeToCeiling = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);
        SurfaceRules.ConditionSource netherrack = SurfaceRules.noiseCondition2d(Noises.NETHERRACK, 0.54);
        SurfaceRules.ConditionSource netherWart = SurfaceRules.noiseCondition2d(Noises.NETHER_WART, 1.17);
        SurfaceRules.ConditionSource netherStateSelector = SurfaceRules.noiseCondition2d(Noises.NETHER_STATE_SELECTOR, 0.0);
        SurfaceRules.ConditionSource lavaLake = SurfaceRules.noiseCondition2d(ModNoises.NETHER_LAVA_LAKE, 0.3);
        SurfaceRules.ConditionSource belowLakeLevel = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(mapY.get(-1)), 0);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(closeToCeiling, NETHERRACK),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(belowLakeLevel, SurfaceRules.ifTrue(lavaLake, LAVA))
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.BASALT_DELTAS),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, BASALT),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(netherStateSelector, BASALT), BLACKSTONE))
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(biomes, Biomes.SOUL_SAND_VALLEY),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, SurfaceRules.sequence(SurfaceRules.ifTrue(netherStateSelector, SOUL_SAND), SOUL_SOIL)),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(netherStateSelector, SOUL_SAND), SOUL_SOIL))
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, Biomes.WARPED_FOREST),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.not(netherrack),
                                                atHeight(mapY.get(-1), SurfaceRules.sequence(SurfaceRules.ifTrue(netherWart, WARPED_WART_BLOCK), WARPED_NYLIUM))
                                        )
                                ),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, Biomes.CRIMSON_FOREST),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.not(netherrack),
                                                atHeight(mapY.get(-1), SurfaceRules.sequence(SurfaceRules.ifTrue(netherWart, NETHER_WART_BLOCK), CRIMSON_NYLIUM))
                                        )
                                )
                        )
                ),
                NETHERRACK
        );
    }

    private static SurfaceRules.RuleSource atHeight(int height, SurfaceRules.RuleSource rule) {
        return topToBottomInclusive(height, height, rule);
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

    private static SurfaceRules.RuleSource stateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    @FunctionalInterface
    interface SurfaceYGetter { int get(int i);}
}
