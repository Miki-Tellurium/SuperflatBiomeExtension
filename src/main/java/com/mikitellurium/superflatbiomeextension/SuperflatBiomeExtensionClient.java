package com.mikitellurium.superflatbiomeextension;

import com.mikitellurium.superflatbiomeextension.client.DebugOverlay;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class SuperflatBiomeExtensionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			HudElementRegistry.addLast(FastId.ofMod("debug"), new DebugOverlay());
		}
	}
}