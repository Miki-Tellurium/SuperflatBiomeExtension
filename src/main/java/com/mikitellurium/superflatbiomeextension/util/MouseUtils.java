package com.mikitellurium.superflatbiomeextension.util;

import net.minecraft.client.util.math.Rect2i;

public class MouseUtils {
    public static boolean isAboveArea(double mouseX, double mouseY, int xPos, int yPos, int sizeX, int sizeY) {
        return (mouseX >= xPos && mouseX <= xPos + sizeX) && (mouseY >= yPos && mouseY <= yPos + sizeY);
    }

    public static boolean isAboveArea(double mouseX, double mouseY, Rect2i area) {
        return isAboveArea(mouseX, mouseY, area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }
}
