package com.mikitellurium.superflatbiomeextension.registry;

public class ModRegistries {
    public static void register() {
        GenerationShapeConfigRegistry.init();
        ModChunkGenerators.register();
        ModMultiNoiseBiomeSourceParameterLists.register();
    }
}
