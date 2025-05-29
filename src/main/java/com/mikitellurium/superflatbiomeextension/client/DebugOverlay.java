package com.mikitellurium.superflatbiomeextension.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Locale;

public class DebugOverlay implements LayeredDrawer.Layer {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private int y = 0;

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!shouldShowDebugHud()) return;
        y = 2;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        ClientWorld world = minecraft.world;
        ClientPlayerEntity player = minecraft.player;
        if (world != null && player != null) {
            RegistryEntry<Biome> biome = world.getBiome(player.getBlockPos());
            write(context,
                    biome.getIdAsString(),
                    String.format(Locale.ROOT, "X: %.3f", player.getX()),
                    String.format(Locale.ROOT, "Y: %.3f", player.getY()),
                    String.format(Locale.ROOT, "Z: %.3f", player.getZ())
            );
        }
    }

    private void write(DrawContext context, String... texts) {
        Arrays.stream(texts).forEach((text) -> {
            context.drawText(textRenderer, text, 5, y, -1, false);
            y += textRenderer.fontHeight + 1;
        });
    }

    private boolean shouldShowDebugHud() {
        return !MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud();
    }
}
