package com.mikitellurium.superflatbiomeextension.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Arrays;
import java.util.Locale;

public class DebugOverlay implements HudElement {
    private int y = 0;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        y = 2;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel world = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (world != null && player != null) {
            Holder<Biome> biome = world.getBiome(player.blockPosition());
            Direction direction = player.getDirection();
            write(graphics,
                    biome.getRegisteredName(),
                    String.format(Locale.ROOT, "X: %.3f", player.getX()),
                    String.format(Locale.ROOT, "Y: %.3f", player.getY()),
                    String.format(Locale.ROOT, "Z: %.3f", player.getZ()),
                    String.format(Locale.ROOT, "Facing: %s", direction.getSerializedName())
            );
        }
    }

    private void write(GuiGraphicsExtractor graphics, String... texts) {
        Font textRenderer = Minecraft.getInstance().font;
        Arrays.stream(texts).forEach((text) -> {
            graphics.text(textRenderer, text, 5, y, -1);
            y += textRenderer.lineHeight + 1;
        });
    }
}
