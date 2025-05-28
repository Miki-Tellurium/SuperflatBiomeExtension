package com.mikitellurium.superflatbiomeextension.worldgen.noise;

import com.mikitellurium.superflatbiomeextension.SuperflatBiomeExtension;
import com.mikitellurium.superflatbiomeextension.mixin.StructureAccessors;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class CustomFlatBerdifier implements DensityFunction.NoisePos {
    private static final Identifier WORLDGEN_REGION_RANDOM = FastId.ofMc("worldgen_region_random");
    private final ChunkPos chunkPos;
    private final Blender blender;
    private final BlockPos.Mutable pos = new BlockPos.Mutable();
    private final StructureWeightSampler structureWeightSampler;
    private final double threshold;
    private final Function<BlockPos, BlockState> stateFunction;

    public static CustomFlatBerdifier create(ChunkPos chunkPos, Blender blender, net.minecraft.world.gen.StructureAccessor structureAccessor, NoiseConfig noiseConfig, Function<BlockPos, BlockState> stateFunction, Function<Random, Double> thresholdFunction) {
        Random random = noiseConfig.getOrCreateRandomDeriver(WORLDGEN_REGION_RANDOM).split(chunkPos.getStartPos());
        return new CustomFlatBerdifier(chunkPos, blender, createFilteredWeightSampler(structureAccessor, chunkPos), stateFunction, thresholdFunction.apply(random));
    }

    public static StructureWeightSampler createFilteredWeightSampler(net.minecraft.world.gen.StructureAccessor accessor, ChunkPos pos) {
        int i = pos.getStartX();
        int j = pos.getStartZ();
        ObjectList<StructureWeightSampler.Piece> pieceList = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> junctionList = new ObjectArrayList<>(32);
        accessor.getStructureStarts(pos, (structure) -> {
            if (structure instanceof JigsawStructure) {
                String jigsawName = ((StructureAccessors.Jigsaw)structure).getStartJigsawName().map(Identifier::getPath).orElse(StringUtils.EMPTY);
                return structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE && jigsawName.equals("city_anchor");
            }
            return false;
        }).forEach(
                start -> {
                    StructureTerrainAdaptation structureTerrainAdaptation = start.getStructure().getTerrainAdaptation();

                    for (StructurePiece structurePiece : start.getChildren()) {
                        if (structurePiece.intersectsChunk(pos, 12)) {
                            if (structurePiece instanceof PoolStructurePiece poolStructurePiece) {
                                StructurePool.Projection projection = poolStructurePiece.getPoolElement().getProjection();
                                if (projection == StructurePool.Projection.RIGID) {
                                    pieceList.add(
                                            new StructureWeightSampler.Piece(poolStructurePiece.getBoundingBox(), structureTerrainAdaptation, poolStructurePiece.getGroundLevelDelta())
                                    );
                                }

                                for (JigsawJunction jigsawJunction : poolStructurePiece.getJunctions()) {
                                    int ix = jigsawJunction.getSourceX();
                                    int jx = jigsawJunction.getSourceZ();
                                    if (ix > i - 12 && jx > j - 12 && ix < i + 15 + 12 && jx < j + 15 + 12) {
                                        junctionList.add(jigsawJunction);
                                    }
                                }
                            } else {
                                pieceList.add(new StructureWeightSampler.Piece(structurePiece.getBoundingBox(), structureTerrainAdaptation, 0));
                            }
                        }
                    }
                }
        );
        return new StructureWeightSampler(pieceList.iterator(), junctionList.iterator());
    }

    public CustomFlatBerdifier(ChunkPos chunkPos, Blender blender, StructureWeightSampler sampler, Function<BlockPos, BlockState> stateFunction, double threshold) {
        this.chunkPos = chunkPos;
        this.blender = blender;
        this.structureWeightSampler = sampler;
        this.stateFunction = stateFunction;
        this.threshold = threshold;
    }

    // Always updatePosition() before sampling
    public BlockState sampleBlockState() {
        double sample = this.sample();
        if (sample <= threshold) {
            SuperflatBiomeExtension.logger().info("Sample= {}, Threshold= {}", sample, threshold);
        }
        return sample > threshold ? stateFunction.apply(this.pos.toImmutable()) : Blocks.CAVE_AIR.getDefaultState();
    }

    public double sample() {
        return structureWeightSampler.sample(this);
    }

    public void updatePosition(Vec3i pos) {
        this.pos.set(pos);
    }

    @Override
    public int blockX() {
        return chunkPos.getStartX() + pos.getX();
    }

    @Override
    public int blockY() {
        return pos.getY();
    }

    @Override
    public int blockZ() {
        return chunkPos.getStartZ() + pos.getZ();
    }

    @Override
    public Blender getBlender() {
        return this.blender;
    }

    private static double getRandomThreshold(Random random) {
        return -0.1 + random.nextDouble() * 0.01;
    }
}
