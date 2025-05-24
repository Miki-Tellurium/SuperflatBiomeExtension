package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.SuperflatBiomeExtension;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructurePlacementData;shouldIgnoreEntities()Z", shift = At.Shift.BEFORE))
    private void inject$replaceBlocks(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags, CallbackInfoReturnable<Boolean> cir) {

    }
}
