package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ServerChunkLoadingManagerMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/noise/NoiseConfig;create(Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;Lnet/minecraft/registry/RegistryEntryLookup;J)Lnet/minecraft/world/gen/noise/NoiseConfig;", ordinal = 1))
    private NoiseConfig wrapOperation$setNoiseConfig(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup, long seed, Operation<NoiseConfig> original, @Local(argsOnly = true) ChunkGenerator chunkGenerator, @Local DynamicRegistryManager registryManager) {
        ChunkGeneratorSettings settings;
        if (chunkGenerator instanceof FlatBiomeExtendedChunkGenerator generator) {
            settings = generator.getConfig().getChunkGeneratorSettings();
        } else {
            settings = chunkGeneratorSettings;
        }
        return original.call(settings, noiseParametersLookup, seed);
    }
}
