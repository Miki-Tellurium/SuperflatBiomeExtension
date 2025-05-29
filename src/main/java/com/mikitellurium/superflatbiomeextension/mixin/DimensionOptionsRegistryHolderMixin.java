package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import net.minecraft.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionOptionsRegistryHolder.class)
public class DimensionOptionsRegistryHolderMixin {
    /*
     * Vanilla renders the darker sky at low y values unless the special property is flat,
     * this set the world special property to flat to avoid that.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "getSpecialProperty", at = @At(value = "RETURN"), cancellable = true)
    private static void inject$setSpecialProperty(Registry<DimensionOptions> registry, CallbackInfoReturnable<LevelProperties.SpecialProperty> cir) {
        LevelProperties.SpecialProperty specialProperty = registry.getOptionalValue(DimensionOptions.OVERWORLD)
                .filter(options -> options.chunkGenerator() instanceof FlatBiomeExtendedChunkGenerator)
                .map(options -> LevelProperties.SpecialProperty.FLAT)
                .orElse(cir.getReturnValue());
        cir.setReturnValue(specialProperty);
    }
}
