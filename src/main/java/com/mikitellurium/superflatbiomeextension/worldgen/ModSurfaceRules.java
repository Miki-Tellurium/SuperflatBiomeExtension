package com.mikitellurium.superflatbiomeextension.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class ModSurfaceRules {
    private static final MaterialRules.MaterialRule TERRACOTTA = block(Blocks.TERRACOTTA);
    private static final MaterialRules.MaterialRule RED_SAND = block(Blocks.RED_SAND);
    private static final MaterialRules.MaterialRule RED_SANDSTONE = block(Blocks.RED_SANDSTONE);
    private static final MaterialRules.MaterialRule STONE = block(Blocks.STONE);
    private static final MaterialRules.MaterialRule DEEPSLATE = block(Blocks.DEEPSLATE);
    private static final MaterialRules.MaterialRule DIRT = block(Blocks.DIRT);
    private static final MaterialRules.MaterialRule PODZOL = block(Blocks.PODZOL);
    private static final MaterialRules.MaterialRule COARSE_DIRT = block(Blocks.COARSE_DIRT);
    private static final MaterialRules.MaterialRule MYCELIUM = block(Blocks.MYCELIUM);
    private static final MaterialRules.MaterialRule GRASS_BLOCK = block(Blocks.GRASS_BLOCK);
    private static final MaterialRules.MaterialRule GRAVEL = block(Blocks.GRAVEL);
    private static final MaterialRules.MaterialRule SAND = block(Blocks.SAND);
    private static final MaterialRules.MaterialRule SANDSTONE = block(Blocks.SANDSTONE);
    private static final MaterialRules.MaterialRule PACKED_ICE = block(Blocks.PACKED_ICE);
    private static final MaterialRules.MaterialRule SNOW_BLOCK = block(Blocks.SNOW_BLOCK);
    private static final MaterialRules.MaterialRule MUD = block(Blocks.MUD);
    private static final MaterialRules.MaterialRule POWDER_SNOW = block(Blocks.POWDER_SNOW);
    private static final MaterialRules.MaterialRule MOSS_BLOCK = block(Blocks.MOSS_BLOCK);
    private static final MaterialRules.MaterialRule ICE = block(Blocks.ICE);
    private static final MaterialRules.MaterialRule WATER = block(Blocks.WATER);

    private static final MaterialRules.MaterialRule LAVA = block(Blocks.LAVA);
    private static final MaterialRules.MaterialRule NETHERRACK = block(Blocks.NETHERRACK);
    private static final MaterialRules.MaterialRule SOUL_SAND = block(Blocks.SOUL_SAND);
    private static final MaterialRules.MaterialRule SOUL_SOIL = block(Blocks.SOUL_SOIL);
    private static final MaterialRules.MaterialRule BASALT = block(Blocks.BASALT);
    private static final MaterialRules.MaterialRule BLACKSTONE = block(Blocks.BLACKSTONE);
    private static final MaterialRules.MaterialRule WARPED_WART_BLOCK = block(Blocks.WARPED_WART_BLOCK);
    private static final MaterialRules.MaterialRule WARPED_NYLIUM = block(Blocks.WARPED_NYLIUM);
    private static final MaterialRules.MaterialRule NETHER_WART_BLOCK = block(Blocks.NETHER_WART_BLOCK);
    private static final MaterialRules.MaterialRule CRIMSON_NYLIUM = block(Blocks.CRIMSON_NYLIUM);

    private static final MaterialRules.MaterialRule END_STONE = block(Blocks.END_STONE);

    public static MaterialRules.MaterialRule createDefaultModSurfaceRule(int surfaceY, boolean generateWater) {
        SurfaceYGetter mapY = (i) -> surfaceY + i;
        ImmutableList.Builder<MaterialRules.MaterialRule> builder = new ImmutableList.Builder<>();
        MaterialRules.MaterialRule grassRule = MaterialRules.sequence(
                topToBottomInclusive(mapY.get(-1), mapY.get(-1), GRASS_BLOCK),
                topToBottomInclusive(mapY.get(-2), mapY.get(-3), DIRT)
        );
        MaterialRules.MaterialRule waterRule = MaterialRules.condition(
                MaterialRules.water(mapY.get(0), mapY.get(0)),
                MaterialRules.condition(MaterialRules.aboveY(YOffset.fixed(mapY.get(-3)), 0), SAND)
        );
        MaterialRules.MaterialRule sandFloorRule = MaterialRules.condition(
                MaterialRules.water(mapY.get(0), mapY.get(0)),
                MaterialRules.condition(MaterialRules.aboveY(YOffset.fixed(mapY.get(-3)), 0), SAND));
        MaterialRules.MaterialRule gravelFloorRule = MaterialRules.condition(
                MaterialRules.water(mapY.get(0), mapY.get(0)),
                MaterialRules.condition(MaterialRules.aboveY(YOffset.fixed(mapY.get(-3)), 0), GRAVEL));
        MaterialRules.MaterialCondition materialCondition1 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.909, -0.5454);
        MaterialRules.MaterialCondition materialCondition2 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.1818, 0.1818);
        MaterialRules.MaterialCondition materialCondition3 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 0.5454, 0.909);
        MaterialRules.MaterialRule powderSnowRule1 = MaterialRules.condition(
                MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.45, 0.58), POWDER_SNOW);
        MaterialRules.MaterialRule powderSnowRule2 = MaterialRules.condition(
                MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.35, 0.6), POWDER_SNOW);
        MaterialRules.MaterialRule gravelRule = MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, STONE), GRAVEL);
        MaterialRules.MaterialRule materialRule = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.DESERT),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), SAND),
                                topToBottomInclusive(mapY.get(-4), mapY.get(-10), SANDSTONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS),
                        MaterialRules.condition(
                                MaterialRules.aboveY(YOffset.fixed(mapY.get(-12)), 0),
                                MaterialRules.sequence(
                                        MaterialRules.condition(materialCondition1,
                                                MaterialRules.sequence(
                                                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), RED_SAND),
                                                        topToBottomInclusive(mapY.get(-4), mapY.get(-10), RED_SANDSTONE)
                                                )),
                                        MaterialRules.condition(materialCondition1, TERRACOTTA),
                                        MaterialRules.condition(materialCondition2, TERRACOTTA),
                                        MaterialRules.condition(materialCondition3, TERRACOTTA),
                                        MaterialRules.terracottaBands(), RED_SANDSTONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WOODED_BADLANDS),
                        MaterialRules.condition(
                                MaterialRules.aboveY(YOffset.fixed(mapY.get(-2)), 0),
                                MaterialRules.sequence(
                                        MaterialRules.condition(materialCondition1, COARSE_DIRT),
                                        MaterialRules.condition(materialCondition2, COARSE_DIRT),
                                        MaterialRules.condition(materialCondition3, COARSE_DIRT),
                                        grassRule
                                )
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.RIVER, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeKeys.WARM_OCEAN),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? WATER : SAND),
                                sandFloorRule
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.FROZEN_RIVER),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? ICE : SAND),
                                sandFloorRule
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.OCEAN, BiomeKeys.DEEP_OCEAN, BiomeKeys.COLD_OCEAN, BiomeKeys.DEEP_COLD_OCEAN),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? WATER : GRAVEL),
                                gravelFloorRule
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.FROZEN_OCEAN, BiomeKeys.DEEP_FROZEN_OCEAN),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), generateWater ? ICE : GRAVEL),
                                gravelFloorRule
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.BEACH, BiomeKeys.SNOWY_BEACH),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), SAND),
                                topToBottomInclusive(mapY.get(-4), mapY.get(-5), SANDSTONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.STONY_SHORE, BiomeKeys.STONY_PEAKS),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA),
                        MaterialRules.sequence(
                                MaterialRules.condition(surfaceNoiseThreshold(1.75), COARSE_DIRT),
                                MaterialRules.condition(surfaceNoiseThreshold(-0.95), PODZOL))
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP),
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP),
                                        topToBottomInclusive(mapY.get(-2), mapY.get(-4), MUD)
                                ),
                                MaterialRules.condition(
                                        MaterialRules.aboveY(YOffset.fixed(mapY.get(-1)), 0),
                                        MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE_SWAMP, 0.0), generateWater ? WATER : MUD)
                                )
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.SWAMP),
                        MaterialRules.condition(
                                MaterialRules.aboveY(YOffset.fixed(mapY.get(-1)), 0),
                                MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE_SWAMP, 0.0), generateWater ? WATER : GRASS_BLOCK)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-3),
                                MaterialRules.sequence(
                                        MaterialRules.condition(surfaceNoiseThreshold(2.0), gravelRule),
                                        MaterialRules.condition(surfaceNoiseThreshold(1.0), STONE),
                                        MaterialRules.condition(surfaceNoiseThreshold(-1.0), DIRT),
                                        gravelRule
                                )
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.SNOWY_SLOPES),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), powderSnowRule1),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), powderSnowRule2),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK),
                                topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.JAGGED_PEAKS),
                        MaterialRules.sequence(
                                topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK),
                                topToBottomInclusive(mapY.get(-2), mapY.get(-3), STONE)
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.GROVE),
                        MaterialRules.condition(
                                MaterialRules.aboveY(YOffset.fixed(mapY.get(-3)), 0),
                                powderSnowRule1
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.FROZEN_PEAKS),
                        MaterialRules.condition(
                                MaterialRules.aboveY(YOffset.fixed(mapY.get(-12)), 0),
                                MaterialRules.sequence(
                                        MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.PACKED_ICE, 0.0, 0.2), PACKED_ICE),
                                        MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.ICE, 0.0, 0.025), ICE),
                                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                                )
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.ICE_SPIKES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), SNOW_BLOCK)
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.DRIPSTONE_CAVES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-3), STONE)
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.LUSH_CAVES),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), MOSS_BLOCK)
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.DEEP_DARK), DEEPSLATE
                ),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.MUSHROOM_FIELDS),
                        topToBottomInclusive(mapY.get(-1), mapY.get(-1), MYCELIUM)
                ),
                grassRule,
                waterRule
        );
        builder.add(materialRule);
        return MaterialRules.sequence(builder.build().toArray(MaterialRules.MaterialRule[]::new));
    }


    private static MaterialRules.MaterialRule topToBottomInclusive(int top, int bottom, MaterialRules.MaterialRule rule) {
        return MaterialRules.condition(
                MaterialRules.aboveY(YOffset.fixed(bottom), 0),
                MaterialRules.condition(
                        MaterialRules.not(MaterialRules.aboveY(YOffset.fixed(top + 1), 0)), rule));
    }

    private static MaterialRules.MaterialCondition surfaceNoiseThreshold(double min) {
        return MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, min / 8.25, Double.MAX_VALUE);
    }

    private static MaterialRules.MaterialRule block(Block block) {
        return MaterialRules.block(block.getDefaultState());
    }

    @FunctionalInterface
    interface SurfaceYGetter {
        int get(int i);
    }
}
