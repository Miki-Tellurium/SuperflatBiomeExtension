package com.mikitellurium.superflatbiomeextension.client;

import com.mikitellurium.superflatbiomeextension.registry.ModMultiNoiseBiomeSourceParameterLists;
import com.mikitellurium.superflatbiomeextension.registry.ModWorldPresets;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatChunkGenerator;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatGeneratorConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;
import java.util.Optional;

public class ModPresetEditors {
    public static final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(
            Optional.of(ModWorldPresets.FLAT_BIOME_EXTENDED), ModPresetEditors::createEditScreen
    );

    private static Screen createEditScreen(CreateWorldScreen parent, WorldCreationContext settings) {
        ChunkGenerator generator = settings.selectedDimensions().overworld();
        RegistryAccess registryAccess = settings.worldgenLoadContext();
        CustomFlatGeneratorConfig config = generator instanceof CustomFlatChunkGenerator customGenerator
                ? customGenerator.getConfig() : CustomFlatGeneratorConfig.createDefault(registryAccess);
        return new EditCustomFlatLevelScreen(parent, config, (newConfig) -> {
            ChunkGenerator newGenerator = createFlatGenerator(newConfig, registryAccess);
            parent.getUiState().updateDimensions((lookup, dimensions) -> dimensions.replaceOverworldGenerator(lookup, newGenerator));
        });
    }

    private static ChunkGenerator createFlatGenerator(CustomFlatGeneratorConfig config, RegistryAccess registryAccess) {
        ResourceKey<MultiNoiseBiomeSourceParameterList> key = config.reduceUndergroundBiomes() ?
                ModMultiNoiseBiomeSourceParameterLists.REDUCED_UNDERGROUND_BIOMES : MultiNoiseBiomeSourceParameterLists.OVERWORLD;
        Holder<MultiNoiseBiomeSourceParameterList> parameterList = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(key);
        return new CustomFlatChunkGenerator(MultiNoiseBiomeSource.createFromPreset(parameterList), config);
    }
}
