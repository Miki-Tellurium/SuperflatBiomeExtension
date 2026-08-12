package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MultiNoiseBiomeSourceParameterList.Preset.class)
public interface MultiNoiseBiomeSourceParameterListPresetAccessor {
    @Accessor static Map<Identifier, MultiNoiseBiomeSourceParameterList.Preset> getBY_NAME() {return null;}
}
