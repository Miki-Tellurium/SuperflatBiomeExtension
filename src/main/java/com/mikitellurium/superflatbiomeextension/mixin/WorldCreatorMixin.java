package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.client.EditCustomFlatLevelScreen;
import com.mikitellurium.superflatbiomeextension.registry.ModMultiNoiseBiomeSourceParameterLists;
import com.mikitellurium.superflatbiomeextension.registry.ModWorldPresets;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatGeneratorConfig;
import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreator.class)
public abstract class WorldCreatorMixin {
    @Shadow public abstract WorldCreator.WorldType getWorldType();

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getLevelScreenProvider", at = @At(value = "HEAD"), cancellable = true)
    private void inject$returnFlatScreenProvider(CallbackInfoReturnable<LevelScreenProvider> cir) {
        RegistryEntry<WorldPreset> registryEntry = this.getWorldType().preset();
        if (registryEntry.matches((key) -> key.equals(ModWorldPresets.FLAT_BIOME_EXTENDED))) {
            cir.setReturnValue((parent, generatorOptionsHolder) -> {
                ChunkGenerator chunkGenerator = generatorOptionsHolder.selectedDimensions().getChunkGenerator();
                CustomFlatGeneratorConfig customFlatGeneratorConfig = chunkGenerator instanceof CustomFlatChunkGenerator ? ((CustomFlatChunkGenerator) chunkGenerator).getConfig() : CustomFlatGeneratorConfig.createDefault();
                return new EditCustomFlatLevelScreen(parent, customFlatGeneratorConfig,
                        (config) -> parent.getWorldCreator().applyModifier(createModifier(config))
                );
            });
        }
    }

    @Unique
    private static GeneratorOptionsHolder.RegistryAwareModifier createModifier(CustomFlatGeneratorConfig config) {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> lookup = dynamicRegistryManager.getOrThrow(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            RegistryEntry<MultiNoiseBiomeSourceParameterList> parameters = lookup.getOrThrow(ModMultiNoiseBiomeSourceParameterLists.OVERWORLD_FLAT);
            BiomeSource biomeSource = MultiNoiseBiomeSource.create(parameters);
            ChunkGenerator chunkGenerator = new CustomFlatChunkGenerator(biomeSource, config);
            return dimensionsRegistryHolder.with(dynamicRegistryManager, chunkGenerator);
        };
    }
}
