package com.mikitellurium.superflatbiomeextension.registry;

public class ModRegistries {
    public static void register() {
        ModSurfaceRuleProviders.register();
        NoiseSettingsRegistry.register();
        ModChunkGenerators.register();
        ModMultiNoiseBiomeSourceParameterLists.register();
        ModSurfaceRules.register();
    }
}
