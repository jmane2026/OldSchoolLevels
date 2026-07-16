package com.jmane2026.oldschoollevels.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class DamageIndicatorManager {
    private static final List<DamageTicket> TICKETS = new ArrayList<>();
    private static final int MAX_TICKS = 40;

    private static class DamageTicket {
        float amount;
        int ticks;
        float xPos, yPos;
        float vx, vy;
        boolean isCrit;
        boolean isIncoming;

        public DamageTicket(float amount, boolean isCrit, boolean isIncoming, float startX, float startY) {
            this.amount = amount;
            this.isCrit = isCrit;
            this.isIncoming = isIncoming;
            this.ticks = MAX_TICKS;
            this.xPos = startX;
            this.yPos = startY;

            // Randomize trajectory slightly for "scatter" effect
            float randX = (float) Math.random() * 1.5f;
            this.vx = isIncoming ? -1.2f - randX : 1.2f + randX;
            this.vy = -1.5f - (float) Math.random(); // Much lower upward burst for gradual slope
        }

        public void tick() {
            this.ticks--;
            this.xPos += vx;
            this.yPos += vy;
            this.vy += 0.1f; // Lower gravity for a more floaty, horizontal arch
            this.vx *= 0.95f; // Friction
        }
    }

    public static void add(float amount, boolean isCrit, boolean isIncoming) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        // Outgoing spawns right, Incoming spawns left
        float startX = isIncoming ? centerX - 45f : centerX + 20f;
        float startY = centerY + 5f; // Start slightly below crosshair for better visibility

        TICKETS.add(new DamageTicket(amount, isCrit, isIncoming, startX, startY));
    }

    public static void clientTick() {
        TICKETS.forEach(DamageTicket::tick);
        TICKETS.removeIf(ticket -> ticket.ticks <= 0);
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        for (DamageTicket ticket : TICKETS) {
            String text = String.format("%.1f", ticket.amount);
            int color = 0xFFFF0000; // Unified Red for all damage types for visibility

            // Critical hit scaling
            float scale = ticket.isCrit ? 4.0f : 2.0f; // Base size is 2.0, Crit is now 2x base (4.0)
            if (ticket.isCrit) color = 0xFFFFD700; // Gold for crits

            graphics.pose().pushMatrix();
            // Interpolate position for smooth motion
            float renderX = ticket.xPos + (ticket.vx * partialTick);
            float renderY = ticket.yPos + (ticket.vy * partialTick);

            graphics.pose().translate(renderX, renderY, graphics.pose());
            graphics.pose().scale(scale, scale, graphics.pose());

            // Render text centered on the trajectory point
            int xOffset = -(mc.font.width(text) / 2);
            graphics.text(mc.font, text, xOffset, 0, color); // Main text (Full opacity)

            graphics.pose().popMatrix();
        }
    }
}