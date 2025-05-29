package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.GeodeFeature;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.IceSpikeFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
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
        @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"), cancellable = true)
        private void inject$fixGenerateHeight(FeatureContext<GeodeFeatureConfig> context, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockPos pos) {
            if (context.getGenerator() instanceof FlatBiomeExtendedChunkGenerator flatGenerator) {
                if (pos.getY() > flatGenerator.getSeaLevel() - 7) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
    @Mixin(IceSpikeFeature.class)
    public static class IceSpike {
        /*
         * Ice spikes only generate the packed ice pillar if the block pos y is above 50.
         * This return 51 to allow ice spikes to generate the pillar in flat worlds where the sea level is below 50.
         */
        @WrapOperation(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I", ordinal = 1))
        private int inject$fixPillarPlacement(BlockPos blockPos, Operation<Integer> original, @Local(argsOnly = true)FeatureContext<DefaultFeatureConfig> context) {
            if (context.getGenerator() instanceof FlatBiomeExtendedChunkGenerator flatGenerator) {
                if (blockPos.getY() >= flatGenerator.getSeaLevel()) {
                    return original.call(new BlockPos(0, 51, 0));
                }
            }
            return original.call(blockPos);
        }
    }
}
