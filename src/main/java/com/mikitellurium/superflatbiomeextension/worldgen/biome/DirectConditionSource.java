package com.mikitellurium.superflatbiomeextension.worldgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record DirectConditionSource(boolean condition) implements SurfaceRules.ConditionSource {
    public static final MapCodec<DirectConditionSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                        Codec.BOOL.fieldOf("condition").forGetter(DirectConditionSource::condition)
                    ).apply(instance, DirectConditionSource::new)
    );

    public static DirectConditionSource of(boolean condition) {
        return new DirectConditionSource(condition);
    }

    @Override
    public MapCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        return () -> condition;
    }
}
