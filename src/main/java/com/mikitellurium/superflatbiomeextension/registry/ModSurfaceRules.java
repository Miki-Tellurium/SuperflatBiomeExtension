package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.DirectConditionSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModSurfaceRules {
    public static void register() {
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, FastId.ofMod("direct"), DirectConditionSource.CODEC);
    }
}
