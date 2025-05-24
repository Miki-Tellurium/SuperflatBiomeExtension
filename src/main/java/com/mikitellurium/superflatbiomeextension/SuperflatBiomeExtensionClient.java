package com.mikitellurium.superflatbiomeextension;

import com.mikitellurium.superflatbiomeextension.client.DebugOverlay;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

public class SuperflatBiomeExtensionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudLayerRegistrationCallback.EVENT.register((wrapper) -> wrapper.addLayer(IdentifiedLayer.of(FastId.ofMod("debug"), new DebugOverlay())));
	}
}