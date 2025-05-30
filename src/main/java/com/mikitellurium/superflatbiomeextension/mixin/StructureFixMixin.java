package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.OceanMonumentStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.WoodlandMansionStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

public class StructureFixMixin {
    @Mixin(WoodlandMansionStructure.class)
    public abstract static class WoodlandMansion {
        @Shadow
        protected abstract void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos pos, BlockRotation rotation);
        /*
         * Mansions don't generate if their y coordinate is lower than 60, this allow mansions to
         * generate at any height if the world is custom flat.
         */
        @Inject(method = "getStructurePosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"), cancellable = true)
        private void inject$returnPositionIfFlat(Structure.Context context, CallbackInfoReturnable<Optional<Structure.StructurePosition>> cir, @Local BlockRotation rotation, @Local BlockPos pos) {
            if (context.chunkGenerator() instanceof CustomFlatChunkGenerator) {
                cir.setReturnValue(Optional.of(new Structure.StructurePosition(pos, (collector) -> this.addPieces(collector, context, pos, rotation))));
            }
        }
    }
    @Mixin(OceanMonumentStructure.class)
    public static class OceanMonument {
        /*
         * Monuments always generate at y 39, this makes monuments always generate below sea level in custom flat worlds
         * unless the surface is lower than the monument height which make the monument generate over the sea level.
         */
        @SuppressWarnings("deprecation")
        @Inject(method = "addPieces", at = @At(value = "TAIL"))
        private static void inject$shiftStructure(StructurePiecesCollector collector, Structure.Context context, CallbackInfo ci) {
            ChunkGenerator chunkGenerator = context.chunkGenerator();
            if (chunkGenerator instanceof CustomFlatChunkGenerator) {
                OceanMonumentGenerator.Base base = (OceanMonumentGenerator.Base) collector.toList().pieces().getFirst();
                BlockBox boundingBox = base.getBoundingBox();
                int maxAllowedShift = context.chunkGenerator().getMinimumY() - boundingBox.getMinY() + 2;
                int shift = Math.max(chunkGenerator.getSeaLevel() - boundingBox.getMaxY(), maxAllowedShift);
                base.getBoundingBox().move(0, shift, 0);
                for (OceanMonumentGenerator.Piece piece : ((StructureAccessors.OceanMonumentBase)base).getChildren()) {
                    piece.getBoundingBox().move(0, shift, 0);
                }
            }
        }
    }
}
