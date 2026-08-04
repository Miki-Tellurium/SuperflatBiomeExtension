package com.mikitellurium.superflatbiomeextension;

import com.mikitellurium.superflatbiomeextension.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperflatBiomeExtension implements ModInitializer {
	private static final String MOD_ID = "superflatbiomeextension";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModRegistries.register();
	}

	public static String modId() {
		return MOD_ID;
	}

	public static Logger logger() {
		return LOGGER;
	}
}
