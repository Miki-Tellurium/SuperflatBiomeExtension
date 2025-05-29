package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.mixin.GenerationShapeConfigAccessor;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;

public class GenerationShapeConfigRegistry {
    public static final RegistryKey<Registry<GenerationShapeConfig>> REGISTRY_KEY = RegistryKey.ofRegistry(FastId.ofMod("generation_shape_config"));
    public static final Registry<GenerationShapeConfig> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
    public static final Codec<GenerationShapeConfig> REGISTRY_CODEC = Identifier.CODEC
            .xmap(
                    (id) -> REGISTRY.getOptionalValue(id).orElseThrow(() -> new IllegalArgumentException("Unknown GenerationShapeConfig: " + id)),
                    REGISTRY::getId
            );
    public static final Codec<GenerationShapeConfig> CODEC = Codec.withAlternative(GenerationShapeConfig.CODEC, REGISTRY_CODEC);

    public static final GenerationShapeConfig SURFACE = GenerationShapeConfigAccessor.getSURFACE();
    public static final GenerationShapeConfig NETHER = GenerationShapeConfigAccessor.getNETHER();
    public static final GenerationShapeConfig END = GenerationShapeConfigAccessor.getEND();
    public static final GenerationShapeConfig CAVES = GenerationShapeConfigAccessor.getCAVES();
    public static final GenerationShapeConfig FLOATING_ISLANDS = GenerationShapeConfigAccessor.getFLOATING_ISLANDS();

    public static void init() {
        Registry.register(REGISTRY, FastId.ofMc("surface"), SURFACE);
        Registry.register(REGISTRY, FastId.ofMc("nether"), NETHER);
        Registry.register(REGISTRY, FastId.ofMc("end"), END);
        Registry.register(REGISTRY, FastId.ofMc("caves"), CAVES);
        Registry.register(REGISTRY, FastId.ofMc("floating_islands"), FLOATING_ISLANDS);
    }
}
