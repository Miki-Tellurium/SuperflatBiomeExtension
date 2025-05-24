package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DensityFunctions.class)
public interface DensityFunctionsAccessor {
    @Invoker("createSurfaceNoiseRouter")
    static NoiseRouter invokeCreateSurfaceNoiseRouter(RegistryEntryLookup<DensityFunction> densityFunctionLookup, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup, boolean largeBiomes, boolean amplified) {
        throw new AssertionError();
    }
}
