package com.mikitellurium.superflatbiomeextension.registry;

public class ModRegistries {
    public static void register() {
        NoiseSettingsRegistry.init();
        ModChunkGenerators.register();
        ModMultiNoiseBiomeSourceParameterLists.register();
        ModSurfaceRuleProviders.register();
    }
}
