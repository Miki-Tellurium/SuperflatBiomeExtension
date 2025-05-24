package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GenerationShapeConfig.class)
public interface GenerationShapeConfigAccessor {
    @Accessor
    static GenerationShapeConfig getSURFACE() {
        return null;
    }
}
