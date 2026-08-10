package com.mikitellurium.superflatbiomeextension.worldgen.noise;

import com.mikitellurium.superflatbiomeextension.mixin.StructureAccessors;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

public class FlatStructureBerdifier implements DensityFunctions.BeardifierOrMarker, DensityFunction.FunctionContext {
    private static final Identifier WORLDGEN_REGION_RANDOM = FastId.ofMc("worldgen_region_random");
    private final ChunkPos chunkPos;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final Beardifier beardifier;
    private final double threshold;

    public static FlatStructureBerdifier create(ChunkPos chunkPos, StructureManager structureManager, RandomState randomState,
                                                 Function<RandomSource, Double> thresholdFunction) {
        RandomSource random = randomState.getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM).at(chunkPos.getWorldPosition());
        return new FlatStructureBerdifier(chunkPos, createWeightSampler(structureManager, chunkPos), thresholdFunction.apply(random));
    }

    public static Beardifier createWeightSampler(StructureManager structureManager, ChunkPos pos) {
        int i = pos.getMinBlockX();
        int j = pos.getMinBlockZ();
        ObjectList<Beardifier.Rigid> pieceList = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> junctionList = new ObjectArrayList<>(32);
        BoundingBox anyPieceBoundingBox = null;
        for (StructureStart start : structureManager.startsForStructure(pos, (structure) -> {
            if (structure instanceof JigsawStructure) {
                String jigsawName = ((StructureAccessors.Jigsaw)structure).getStartJigsawName().map(Identifier::getPath).orElse(StringUtils.EMPTY);
                return structure.terrainAdaptation() != TerrainAdjustment.NONE && jigsawName.equals("city_anchor");
            }
            return false;
        })) {
            TerrainAdjustment structureTerrainAdaptation = start.getStructure().terrainAdaptation();
            for (StructurePiece structurePiece : start.getPieces()) {
                if (structurePiece.isCloseToChunk(pos, 12)) {
                    if (structurePiece instanceof PoolElementStructurePiece poolStructurePiece) {
                        StructureTemplatePool.Projection projection = poolStructurePiece.getElement().getProjection();
                        if (projection == StructureTemplatePool.Projection.RIGID) {
                            pieceList.add(
                                    new Beardifier.Rigid(poolStructurePiece.getBoundingBox(), structureTerrainAdaptation, poolStructurePiece.getGroundLevelDelta())
                            );
                            anyPieceBoundingBox = includeBoundingBox(anyPieceBoundingBox, poolStructurePiece.getBoundingBox());
                        }

                        for (JigsawJunction jigsawJunction : poolStructurePiece.getJunctions()) {
                            int ix = jigsawJunction.getSourceX();
                            int jx = jigsawJunction.getSourceZ();
                            if (ix > i - 12 && jx > j - 12 && ix < i + 15 + 12 && jx < j + 15 + 12) {
                                junctionList.add(jigsawJunction);
                                anyPieceBoundingBox = includeBoundingBox(anyPieceBoundingBox, new BoundingBox(new BlockPos(ix, jigsawJunction.getSourceGroundY(), jx)));
                            }
                        }
                    } else {
                        pieceList.add(new Beardifier.Rigid(structurePiece.getBoundingBox(), structureTerrainAdaptation, 0));
                        anyPieceBoundingBox = includeBoundingBox(anyPieceBoundingBox, structurePiece.getBoundingBox());
                    }
                }
            }
        }
        if (anyPieceBoundingBox == null) {
            return Beardifier.EMPTY;
        }
        BoundingBox affectedBox = anyPieceBoundingBox.inflatedBy(24);
        return new Beardifier(List.copyOf(pieceList), List.copyOf(junctionList), affectedBox);
    }

    private static BoundingBox includeBoundingBox(BoundingBox boundingBox, BoundingBox newBox) {
        return boundingBox == null ? newBox : BoundingBox.encapsulating(boundingBox, newBox);
    }

    public FlatStructureBerdifier(ChunkPos chunkPos, Beardifier beardifier, double threshold) {
        this.chunkPos = chunkPos;
        this.beardifier = beardifier;
        this.threshold = threshold;
    }

    // Remember to always updatePosition() before sampling
    public BlockState sampleBlockState(BlockState layerState) {
        return this.sample() > threshold ? layerState : Blocks.CAVE_AIR.defaultBlockState();
    }

    public double sample() {
        return beardifier.compute(this);
    }

    public void updatePosition(BlockPos pos) {
        this.pos.set(pos);
    }

    @Override
    public int blockX() {
        return chunkPos.getMinBlockX() + pos.getX();
    }

    @Override
    public int blockY() {
        return pos.getY();
    }

    @Override
    public int blockZ() {
        return chunkPos.getMinBlockZ() + pos.getZ();
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        return beardifier.compute(context);
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }
}
