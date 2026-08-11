package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldDimensions.class)
public class WorldDimensionsMixin {
    /*
     * Vanilla renders the sky darker at low y values unless the special property is flat
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "specialWorldProperty", at = @At(value = "RETURN"), cancellable = true)
    private static void inject$setSpecialProperty(Registry<LevelStem> registry, CallbackInfoReturnable<PrimaryLevelData.SpecialWorldProperty> cir) {
        PrimaryLevelData.SpecialWorldProperty specialProperty = registry.getOptional(LevelStem.OVERWORLD)
                .filter(stem -> stem.generator() instanceof CustomFlatChunkGenerator)
                .map(stem -> PrimaryLevelData.SpecialWorldProperty.FLAT)
                .orElse(cir.getReturnValue());
        cir.setReturnValue(specialProperty);
    }
}
