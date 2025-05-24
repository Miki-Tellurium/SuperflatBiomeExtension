package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.WoodlandMansionStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WoodlandMansionStructure.class)
public abstract class WoodlandMansionStructureMixin {
    @Shadow protected abstract void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos pos, BlockRotation rotation);

    @Inject(method = "getStructurePosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"), cancellable = true)
    private void inject$returnPositionIfFlat(Structure.Context context, CallbackInfoReturnable<Optional<Structure.StructurePosition>> cir, @Local BlockRotation rotation, @Local BlockPos pos) {
        if (context.chunkGenerator() instanceof FlatBiomeExtendedChunkGenerator) {
            cir.setReturnValue(Optional.of(new Structure.StructurePosition(pos, (collector) -> this.addPieces(collector, context, pos, rotation))));
        }
    }
}
