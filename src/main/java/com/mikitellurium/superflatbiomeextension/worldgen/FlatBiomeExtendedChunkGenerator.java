package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.mixin.ChunkNoiseSamplerAccessor;
import com.mikitellurium.superflatbiomeextension.mixinutil.FlatSurfaceBuilder;
import com.mikitellurium.superflatbiomeextension.worldgen.noise.CustomFlatBerdifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FlatBiomeExtendedChunkGenerator extends ChunkGenerator {
    public static final MapCodec<FlatBiomeExtendedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                            FlatBiomeExtendedGeneratorConfig.CODEC.fieldOf("config").forGetter((generator) -> generator.config)
                    )
                    .apply(instance, instance.stable(FlatBiomeExtendedChunkGenerator::new))
    );

    private final FlatBiomeExtendedGeneratorConfig config;
    private final AquiferSampler.FluidLevelSampler fluidLevelSampler;

    public FlatBiomeExtendedChunkGenerator(BiomeSource biomeSource, FlatBiomeExtendedGeneratorConfig config) {
        super(biomeSource, Util.memoize(config::createGenerationSettings));
        this.config = config;
        this.fluidLevelSampler = (x, y, z) -> new AquiferSampler.FluidLevel(y, Blocks.AIR.getDefaultState());
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    public FlatBiomeExtendedGeneratorConfig getConfig() {
        return this.config;
    }

    @Override
    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        if (this.config.hasStructures()) {
            return super.createStructurePlacementCalculator(structureSetRegistry, noiseConfig, seed);
        }
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, Stream.of());
    }

    private ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.createStructureWeightSampler(world, chunk.getPos()), this.config.getChunkGeneratorSettings(), this.fluidLevelSampler, blender);
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler((c) -> this.createChunkNoiseSampler(c, structureAccessor, blender, noiseConfig));
            BiomeSupplier biomeSupplier = BelowZeroRetrogen.getBiomeSupplier(blender.getBiomeSupplier(this.biomeSource), chunk);
            chunk.populateBiomes(biomeSupplier, ((ChunkNoiseSamplerAccessor) chunkNoiseSampler).invokeCreateMultiNoiseSampler(noiseConfig.getNoiseRouter(), this.config.getChunkGeneratorSettings().spawnTarget()));
            return chunk;
        }, Util.getMainWorkerExecutor().named("init_biomes"));
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk) {
        if (!SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
            HeightContext heightContext = new HeightContext(this, region);
            ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler((c) -> this.createChunkNoiseSampler(c, structureAccessor, Blender.getBlender(region), noiseConfig));
            ChunkGeneratorSettings chunkGeneratorSettings = this.config.getChunkGeneratorSettings();
            SurfaceBuilder surfaceBuilder = noiseConfig.getSurfaceBuilder();
            ((FlatSurfaceBuilder)surfaceBuilder).mixin$setFlat();
            noiseConfig.getSurfaceBuilder().buildSurface(noiseConfig, region.getBiomeAccess(), region.getRegistryManager().getOrThrow(RegistryKeys.BIOME), chunkGeneratorSettings.usesLegacyRandom(), heightContext, chunk, chunkNoiseSampler, chunkGeneratorSettings.surfaceRule());
        }
    }

    @Override
    public int getSpawnHeight(HeightLimitView world) {
        return world.getBottomY() + Math.min(world.getHeight(), 64);
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        final int height = config.getHeight();
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        CustomFlatBerdifier customFlatBerdifier = CustomFlatBerdifier.create(chunk.getPos(), blender, structureAccessor, noiseConfig,
                (blockPos) -> blockPos.getY() == this.getMinimumY() ? Blocks.BEDROCK.getDefaultState() : this.config.getChunkGeneratorSettings().defaultBlock(),
                (random) -> -0.1 + random.nextDouble() * 0.01);

        return CompletableFuture.supplyAsync(() -> {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (int i = 0; i < Math.min(chunk.getHeight(), height); i++) {
                int y = chunk.getBottomY() + i;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        customFlatBerdifier.updatePosition(mutable.set(x, y, z));
                        BlockState blockState = customFlatBerdifier.sampleBlockState();
                        if (!blockState.isAir()) {
                            chunk.setBlockState(mutable, blockState);
                            heightmap.trackUpdate(x, y, z, blockState);
                            heightmap2.trackUpdate(x, y, z, blockState);
                        }
                    }
                }
            }
            return chunk;
        });
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        for (int i = Math.min(this.config.getHeight(), world.getTopYInclusive()); i >= 0; i--) {
            BlockState blockState = this.config.getChunkGeneratorSettings().defaultBlock();
            if (blockState != null && heightmap.getBlockPredicate().test(blockState)) {
                return world.getBottomY() + i;
            }
        }
        return world.getBottomY();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] blockStates = new BlockState[this.config.getHeight()];
        Arrays.fill(blockStates, this.config.getChunkGeneratorSettings().defaultBlock());
        return new VerticalBlockSample(world.getBottomY(), blockStates);
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    @Override
    public int getMinimumY() {
        return this.config.getChunkGeneratorSettings().generationShapeConfig().minimumY();
    }

    @Override
    public int getWorldHeight() {
        return this.config.getChunkGeneratorSettings().generationShapeConfig().height();
    }

    @Override
    public int getSeaLevel() {
        return this.config.getChunkGeneratorSettings().seaLevel();
    }
}
