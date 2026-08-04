package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    /*
     * Vanilla creates an empty RandomState if the chunk generator is not a NoiseBasedChunkGenerator instance.
     * This creates the RandomState correctly for CustomFlatChunkGenerator instances.
     */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;", ordinal = 1))
    private RandomState wrapOperation$setRandomState(NoiseGeneratorSettings noiseGeneratorSettings, HolderGetter<NormalNoise.NoiseParameters> noiseParametersLookup, long seed, Operation<RandomState> original, @Local(argsOnly = true) ChunkGenerator chunkGenerator) {
        NoiseGeneratorSettings settings;
        if (chunkGenerator instanceof CustomFlatChunkGenerator generator) {
            settings = generator.getConfig().getChunkGeneratorSettings();
        } else {
            settings = noiseGeneratorSettings;
        }
        return original.call(settings, noiseParametersLookup, seed);
    }
}
