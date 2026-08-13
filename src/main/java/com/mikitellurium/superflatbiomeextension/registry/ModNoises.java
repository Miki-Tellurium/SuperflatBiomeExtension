package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModNoises {
    public static final ResourceKey<NormalNoise.NoiseParameters> NETHER_LAVA_LAKE = of("nether_lava_lake");

    private static ResourceKey<NormalNoise.NoiseParameters> of(String id) {
        return ResourceKey.create(Registries.NOISE, FastId.ofMod(id));
    }
}
