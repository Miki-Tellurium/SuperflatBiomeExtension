package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

public class StructureFixMixin {
    @Mixin(WoodlandMansionStructure.class)
    public abstract static class WoodlandMansion {
        @Shadow
        private void generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context, BlockPos startPos, Rotation rotation) {
        }
        /*
         * Mansions don't generate if their y coordinate is lower than 60, this allows mansions to
         * generate at any height if the world is custom flat.
         */
        @Inject(method = "findGenerationPoint", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getY()I"), cancellable = true)
        private void inject$returnPositionIfFlat(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir, @Local(name = "rotation") Rotation rotation, @Local(name = "startPos") BlockPos startPos) {
            if (context.chunkGenerator() instanceof CustomFlatChunkGenerator) {
                cir.setReturnValue(Optional.of(new Structure.GenerationStub(startPos, (builder) -> this.generatePieces(builder, context, startPos, rotation))));
            }
        }
    }
    @Mixin(OceanMonumentStructure.class)
    public static class OceanMonument {
        /*
         * Monuments always generate at y 39, this makes monuments always generate below sea level in custom flat worlds
         * unless the surface is lower than the monument height which make the monument generate over the sea level.
         */
        @Inject(method = "generatePieces", at = @At(value = "TAIL"))
        private static void inject$shiftStructure(StructurePiecesBuilder builder, Structure.GenerationContext context, CallbackInfo ci) {
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            if (chunkGenerator instanceof CustomFlatChunkGenerator) {
                StructurePiece base = builder.build().pieces().getFirst();
                BoundingBox boundingBox = base.getBoundingBox();
                int maxAllowedShift = chunkGenerator.getMinY() - boundingBox.minY() + 1; // +1 avoids the monument base being overridden by bedrock if monument generate at bedrock level
                int shift = Math.max(chunkGenerator.getSeaLevel() - boundingBox.maxY(), maxAllowedShift);
                shiftMonument(base, shift);
            }
        }
        /*
         * regeneratePiecesAfterLoad() restores the old bounding box but resets the y coordinate value,
         * so it needs to be shifted again
         */
        @Inject(method = "regeneratePiecesAfterLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePiecesBuilder;<init>()V"))
        private static void inject$restoreBoundingBox(ChunkPos chunkPos, long seed, PiecesContainer savedPieces, CallbackInfoReturnable<PiecesContainer> cir, @Local(name = "oldBoundingBox") BoundingBox oldBoundingBox,  @Local(name = "topPiece") StructurePiece topPiece) {
            BoundingBox box = topPiece.getBoundingBox();
            int shift = oldBoundingBox.minY() - box.minY();
            shiftMonument(topPiece, shift);
        }

        @Unique
        private static void shiftMonument(StructurePiece base, int shift) {
            base.move(0, shift, 0);
            for (StructurePiece piece : ((StructureAccessors.OceanMonumentBase)base).getChildPieces()) {
                piece.move(0, shift, 0);
            }
        }
    }
}
