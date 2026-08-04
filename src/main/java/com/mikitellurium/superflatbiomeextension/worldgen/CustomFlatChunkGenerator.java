package com.mikitellurium.superflatbiomeextension.worldgen;

import com.mikitellurium.superflatbiomeextension.worldgen.noise.CustomFlatBerdifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CustomFlatChunkGenerator extends ChunkGenerator {
    public static final MapCodec<CustomFlatChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                            CustomFlatGeneratorConfig.CODEC.fieldOf("config").forGetter((generator) -> generator.config)
                    )
                    .apply(instance, instance.stable(CustomFlatChunkGenerator::new))
    );
    private final CustomFlatGeneratorConfig config;
    private final net.minecraft.world.level.levelgen.Aquifer.FluidPicker fluidLevelPicker;

    public CustomFlatChunkGenerator(BiomeSource biomeSource, CustomFlatGeneratorConfig config) {
        super(biomeSource, Util.memoize(config::createGenerationSettings));
        this.config = config;
        this.fluidLevelPicker = (x, y, z) -> new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(y, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public CustomFlatGeneratorConfig getConfig() {
        return this.config;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetRegistry, RandomState randomState, long seed) {
        if (this.config.hasStructures()) {
            return super.createState(structureSetRegistry, randomState, seed);
        }
        return ChunkGeneratorStructureState.createForFlat(randomState, seed, this.biomeSource, Stream.of());
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(
                chunk,
                randomState,
                CustomFlatBerdifier.createWeightSampler(structureManager, chunk.getPos()),
                this.config.getChunkGeneratorSettings(),
                this.fluidLevelPicker,
                blender
        );
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess protoChunk) {
        if (!SharedConstants.DEBUG_DISABLE_SURFACE) {
            WorldGenerationContext context = new WorldGenerationContext(this, region);
            Set<Holder<Biome>> possibleBiomes = collectPossibleBiomes(region, 1);
            NoiseChunk noiseChunk = protoChunk.getOrCreateNoiseChunk(chunk -> this.createNoiseChunk(chunk, structureManager, Blender.of(region), randomState));
            NoiseGeneratorSettings chunkGeneratorSettings = this.config.getChunkGeneratorSettings();
            ((com.mikitellurium.superflatbiomeextension.mixinutil.FlatSurfaceBuilder)randomState.surfaceSystem()).mixin$setFlat();
            randomState.surfaceSystem().buildSurface(randomState, region.getBiomeManager(), chunkGeneratorSettings.useLegacyRandomSource(), context, protoChunk, noiseChunk, chunkGeneratorSettings.surfaceRule(), possibleBiomes);
        }
    }

    private static Set<Holder<Biome>> collectPossibleBiomes(WorldGenRegion region, int chunkRadius) {
        Set<Holder<Biome>> chunkBiomes = new it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet<>();
        ChunkPos center = region.getCenter();
        for (int z = center.z() - chunkRadius; z <= center.z() + chunkRadius; z++) {
            for (int x = center.x() - chunkRadius; x <= center.x() + chunkRadius; x++) {
                region.getChunk(x, z).collectBiomesInPalette(chunkBiomes);
            }
        }
        return chunkBiomes;
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor world) {
        return world.getMinY() + Math.min(world.getHeight(), this.config.getTotalHeight());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        List<CustomFlatGeneratorConfig.FlatLayer> layers = config.getResolvedLayers();

        return CompletableFuture.supplyAsync(() -> {
            chunk.getOrCreateNoiseChunk(c -> this.createNoiseChunk(c, structureManager, blender, randomState));
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            if (config.hasCustomLayers()) {
                int yIndex = 0;
                for (CustomFlatGeneratorConfig.FlatLayer layer : layers) {
                    for (int h = 0; h < layer.height() && yIndex < chunk.getHeight(); h++) {
                        int y = this.getMinY() + yIndex++;
                        net.minecraft.world.level.block.state.BlockState state = layer.blockState();
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                mutable.set(x, y, z);
                                chunk.setBlockState(mutable, state);
                                heightmap.update(x, y, z, state);
                                heightmap2.update(x, y, z, state);
                            }
                        }
                    }
                }
            } else {
                final int height = config.getLayerCount();
                CustomFlatBerdifier customFlatBerdifier = CustomFlatBerdifier.create(chunk.getPos(), structureManager, randomState,
                        (blockPos) -> blockPos.getY() == this.getMinY() ? Blocks.BEDROCK.defaultBlockState() : this.config.getChunkGeneratorSettings().defaultBlock(),
                        (random) -> -0.1 + random.nextDouble() * 0.01);
                for (int i = 0; i < Math.min(chunk.getHeight(), height); i++) {
                    int y = this.getMinY() + i;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            customFlatBerdifier.updatePosition(mutable.set(x, y, z));
                            net.minecraft.world.level.block.state.BlockState blockState = customFlatBerdifier.sampleBlockState();
                            if (!blockState.isAir()) {
                                chunk.setBlockState(mutable, blockState);
                                heightmap.update(x, y, z, blockState);
                                heightmap2.update(x, y, z, blockState);
                            }
                        }
                    }
                }
            }
            return chunk;
        });
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState randomState) {
        if (config.hasCustomLayers()) {
            return world.getMinY() + config.getTotalHeight();
        }
        for (int i = Math.min(this.config.getLayerCount(), world.getHeight()); i >= 0; i--) {
            BlockState blockState = this.config.getChunkGeneratorSettings().defaultBlock();
            if (blockState != null && heightmap.isOpaque().test(blockState)) {
                return world.getMinY() + i;
            }
        }
        return world.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState randomState) {
        List<CustomFlatGeneratorConfig.FlatLayer> resolvedLayers = this.config.getResolvedLayers();
        int totalHeight = this.config.getTotalHeight();
        BlockState[] blockStates = new BlockState[totalHeight];
        int yIndex = 0;
        for (CustomFlatGeneratorConfig.FlatLayer layer : resolvedLayers) {
            for (int h = 0; h < layer.height() && yIndex < totalHeight; h++) {
                blockStates[yIndex++] = layer.blockState();
            }
        }
        return new NoiseColumn(world.getMinY(), blockStates);
    }

    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState randomState, net.minecraft.world.level.biome.BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void addDebugScreenInfo(List<String> text, RandomState randomState, BlockPos pos) {
    }

    @Override
    public int getMinY() {
        return this.config.getChunkGeneratorSettings().noiseSettings().minY();
    }

    @Override
    public int getGenDepth() {
        return this.config.getChunkGeneratorSettings().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return this.config.getChunkGeneratorSettings().seaLevel();
    }
}
