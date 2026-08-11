package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PlacedFeatureFixMixin {
    @Mixin(GeodeFeature.class)
    public static class Geode {
        /*
         * Make geodes pierce the surface less often.
         */
        @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"), cancellable = true)
        private void inject$fixGenerateHeight(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> cir, @Local(name = "pos") BlockPos pos) {
            if (context.chunkGenerator() instanceof CustomFlatChunkGenerator flatGenerator) {
                if (pos.getY() > flatGenerator.getSeaLevel() - 7) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
    @Mixin(SpikeFeature.class)
    public static class IceSpike {
        /*
         * Ice spikes only generate the packed ice pillar if the block pos y is above 50.
         * This return 51 to allow ice spikes to generate the pillar in flat worlds where the sea level is below 50.
         */
        @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getY()I", ordinal = 1))
        private int inject$fixPillarPlacement(BlockPos blockPos, Operation<Integer> original, @Local(argsOnly = true, name = "context") FeaturePlaceContext<SpikeConfiguration> context) {
            if (context.chunkGenerator() instanceof CustomFlatChunkGenerator flatGenerator) {
                if (blockPos.getY() >= flatGenerator.getSeaLevel()) {
                    return original.call(new BlockPos(0, 51, 0));
                }
            }
            return original.call(blockPos);
        }
    }
}
