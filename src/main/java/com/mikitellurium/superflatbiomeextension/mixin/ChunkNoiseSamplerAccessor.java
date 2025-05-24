package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerAccessor {
    @Invoker("createMultiNoiseSampler")
    MultiNoiseUtil.MultiNoiseSampler invokeCreateMultiNoiseSampler(NoiseRouter noiseRouter, List<MultiNoiseUtil.NoiseHypercube> spawnTarget);
}
