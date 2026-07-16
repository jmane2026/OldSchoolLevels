package com.jmane2026.oldschoollevels.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class WarningOverlay {
    private static String currentWarning = "";
    private static int warningTicks = 0;
    private static final int MAX_TICKS = 60;

    public static void showWarning(String message) {
        currentWarning = message;
        warningTicks = MAX_TICKS;
    }

    public static void clientTick() {
        if (warningTicks > 0) {
            warningTicks--;
        }
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (warningTicks <= 0 || currentWarning.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;

        // Calculate Y based on whether an inventory is open
        float yPos = 40.0f;
        if (mc.screen instanceof AbstractContainerScreen<?> container) {
            // Just above the inventory frame (guiTop is the top of the actual box)
            yPos = (float) container.getTopPos() - 15.0f;
        }

        // Calculate Alpha for fading
        int alpha = warningTicks < 15 ? (int) (255 * ((warningTicks - partialTick) / 15f)) : 255;
        int color = (alpha << 24) | 0xFF5555; // Reddish

        // Render at the top center, ensuring it's above any open GUI
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)centerX, yPos, graphics.pose());
        graphics.pose().scale(1.2f, 1.2f, graphics.pose());
        graphics.text(mc.font, currentWarning, (-(mc.font.width(currentWarning) / 2)), 0, color);
        graphics.pose().popMatrix();
    }
}