package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.ModSurfaceRuleData;
import com.mikitellurium.superflatbiomeextension.worldgen.biome.SurfaceRuleProvider;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;

public class ModSurfaceRuleProviders {
    public static final ResourceKey<Registry<SurfaceRuleProvider>> REGISTRY_KEY = ResourceKey.createRegistryKey(FastId.ofMod("surface_rule_provider"));
    public static final Registry<SurfaceRuleProvider> REGISTRY = FabricRegistryBuilder.create(REGISTRY_KEY).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();

    public static final SurfaceRuleProvider OVERWORLD_FLAT = ModSurfaceRuleData::overworld;
    public static final SurfaceRuleProvider NETHER_FLAT = ModSurfaceRuleData::nether;
    public static final SurfaceRuleProvider END = (b, y, w) -> SurfaceRuleData.end();

    public static void register() {
        Registry.register(REGISTRY, FastId.ofMod("overworld_flat"), OVERWORLD_FLAT);
        Registry.register(REGISTRY, FastId.ofMod("nether_flat"), NETHER_FLAT);
        Registry.register(REGISTRY, FastId.ofMc("end"), END);
    }
}
