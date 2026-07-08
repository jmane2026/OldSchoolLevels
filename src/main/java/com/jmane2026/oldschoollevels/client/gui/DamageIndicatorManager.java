package com.jmane2026.oldschoollevels.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class DamageIndicatorManager {
    private static class DamageTicket {
        String text;
        int ticks;
        int stagger;
        final int maxTicks = 40; // 2 seconds

        DamageTicket(float amount, int stagger) {
            // Format to 1 decimal place, or whole number if possible
            this.text = amount % 1 == 0 ? String.valueOf((int)amount) : String.format("%.1f", amount);
            this.ticks = maxTicks;
            this.stagger = stagger;
        }
    }

    private static final List<DamageTicket> TICKETS = new ArrayList<>();
    private static final int MAX_TICKS = 40;

    public static void add(double x, double y, double z, float amount) {
        // Calculate stagger based on how many tickets were added in the last few ticks
        int stagger = TICKETS.stream()
                .mapToInt(t -> t.stagger)
                .max()
                .orElse(-1) + 1;

        TICKETS.add(new DamageTicket(amount, stagger));
    }

    public static void clientTick() {
        if (Minecraft.getInstance().isPaused()) return;
        TICKETS.removeIf(ticket -> {
            ticket.ticks--;
            return ticket.ticks <= 0;
        });
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (TICKETS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Center of the screen
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        for (DamageTicket ticket : TICKETS) {
            // Animation: Float up from the center
            float ageProgress = 1.0f - (((float) ticket.ticks - partialTick) / (float) MAX_TICKS);
            
            // Start slightly to the right of crosshair, float up, stagger vertically
            float x = centerX + 10f;
            float y = centerY - 10f - (ageProgress * 40f) - (ticket.stagger * 12f);

            int alpha = ticket.ticks < 10 ? (int) (255 * ((ticket.ticks - partialTick) / 10f)) : 255;
            alpha = Math.max(0, Math.min(255, alpha));
            int color = (alpha << 24) | 0xFF0000; // OSRS Red

            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y, graphics.pose());
            // Make damage numbers slightly larger than XP drops for impact
            graphics.pose().scale(1.2f, 1.2f, graphics.pose());
            
            graphics.text(mc.font, ticket.text, 0, 0, color);
            graphics.pose().popMatrix();
        }
    }
}