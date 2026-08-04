package com.mikitellurium.superflatbiomeextension.registry;

import com.mikitellurium.superflatbiomeextension.util.FastId;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseSettings;

public class GenerationShapeConfigRegistry {
    public static final ResourceKey<Registry<NoiseSettings>> REGISTRY_KEY = ResourceKey.createRegistryKey(FastId.ofMod("generation_shape_config"));
    public static final Registry<NoiseSettings> REGISTRY = FabricRegistryBuilder.create(REGISTRY_KEY).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
    public static final Codec<NoiseSettings> REGISTRY_CODEC = Identifier.CODEC
            .xmap(
                    (id) -> REGISTRY.getOptional(id).orElseThrow(() -> new IllegalArgumentException("Unknown GenerationShapeConfig: " + id)),
                    REGISTRY::getKey
            );
    public static final Codec<NoiseSettings> CODEC = Codec.withAlternative(NoiseSettings.CODEC, REGISTRY_CODEC);

    public static final NoiseSettings SURFACE = NoiseSettings.create(-64, 384, 1, 2);
    public static final NoiseSettings NETHER = NoiseSettings.create(0, 256, 1, 2);
    public static final NoiseSettings END = NoiseSettings.create(0, 256, 1, 2);
    public static final NoiseSettings CAVES = NoiseSettings.create(-64, 384, 1, 2);
    public static final NoiseSettings FLOATING_ISLANDS = NoiseSettings.create(0, 256, 1, 2);

    public static void init() {
        Registry.register(REGISTRY, FastId.ofMc("surface"), SURFACE);
        Registry.register(REGISTRY, FastId.ofMc("nether"), NETHER);
        Registry.register(REGISTRY, FastId.ofMc("end"), END);
        Registry.register(REGISTRY, FastId.ofMc("caves"), CAVES);
        Registry.register(REGISTRY, FastId.ofMc("floating_islands"), FLOATING_ISLANDS);
    }
}
