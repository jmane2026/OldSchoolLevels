package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XpNotificationOverlay {
    private static final List<XpTicket> ACTIVE_NOTIFICATIONS = new ArrayList<>();
    private static final int DISPLAY_DURATION = 60; // 3 seconds

    private static class XpTicket {
        Skill skill;
        long amount;
        long totalXp;
        int ticks;

        XpTicket(Skill skill, long amount, long totalXp) {
            this.skill = skill;
            this.amount = amount;
            this.totalXp = totalXp;
            this.ticks = DISPLAY_DURATION;
        }

        void add(long extra, long newTotal) {
            this.amount += extra;
            this.totalXp = newTotal;
            this.ticks = DISPLAY_DURATION; // Refresh the timer
        }
    }

    public static void notify(Skill skill, long amount, long totalXp) {
        // MERGING LOGIC: Check if we already have a notification for this skill
        for (XpTicket ticket : ACTIVE_NOTIFICATIONS) {
            if (ticket.skill == skill) {
                ticket.add(amount, totalXp);
                return;
            }
        }

        // If not found, add a new one
        ACTIVE_NOTIFICATIONS.add(new XpTicket(skill, amount, totalXp));
    }

    public static void clientTick() {
        Iterator<XpTicket> it = ACTIVE_NOTIFICATIONS.iterator();
        while (it.hasNext()) {
            XpTicket ticket = it.next();
            ticket.ticks--;
            if (ticket.ticks <= 0) {
                it.remove();
            }
        }
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (ACTIVE_NOTIFICATIONS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // POSITIONING: Bottom Left, stacking upwards
        int startX = 10;
        int y = screenHeight - 40;

        for (XpTicket ticket : ACTIVE_NOTIFICATIONS) {
            // Calculate Alpha for a smooth fade out in the last 10 ticks
            float fade = ticket.ticks < 10 ? (ticket.ticks / 10f) : 1.0f;
            int alpha = (int)(fade * 255);
            
            int white = (alpha << 24) | 0xFFFFFF;
            int yellow = (alpha << 24) | 0xFFFF00;
            int gray = (alpha << 24) | 0x333333;
            int barGreen = (alpha << 24) | 0x00FF00;

            // Calculate live level and progress
            int lvl = ExperienceUtils.getLevelAtExperience(ticket.totalXp);
            long currentXp = ExperienceUtils.getXpForLevel(lvl);
            long nextXp = ExperienceUtils.getXpForLevel(lvl + 1);
            float progress = (float) (ticket.totalXp - currentXp) / (nextXp - currentXp);
            if (lvl >= 99) progress = 1.0f;

            graphics.pose().pushMatrix();

            // 0. Define Box Dimensions
            int boxWidth = 120;
            int boxHeight = 30;
            int bgColor = ((int)(fade * 140) << 24); // Semi-transparent black
            // Draw Background Box (Fixing absolute coordinate overflow)
            graphics.fill(startX, y, startX + boxWidth, y + boxHeight, bgColor);
            graphics.outline(startX, y, boxWidth, boxHeight, (alpha << 24) | 0x000000);

            // 1. Draw Icon
            if (ticket.skill.getSpriteIcon() != null) {
                if (ticket.skill == Skill.MAGIC) {
                    // Manual color calculation for alpha fade on raw texture
                    int tint = (alpha << 24) | 0xFFFFFF;
                    
                    // Calculate the current animation frame (0-31)
                    long time = mc.level != null ? mc.level.getGameTime() : 0;
                    int frameIndex = (int) ((time / 2) % 32);
                    int vOffset = frameIndex * 16;

                    graphics.blit(RenderPipelines.GUI_TEXTURED, ticket.skill.getSpriteIcon(), startX + 2, y + 2, 0, vOffset, 16, 16, 16, 16, 16, 512, tint);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, ticket.skill.getSpriteIcon(), startX + 2, y + 2, 16, 16, fade);
                }
            } else {
                graphics.item(ticket.skill.getIcon(), startX + 2, y + 2);
            }

            // 2. Draw Skill Name (Top Left)
            String title = ticket.skill.getDisplayName();
            graphics.text(mc.font, title, startX + 22, y + 4, white);

            // 3. Draw Level (Top Right)
            String lvlText = "Lvl: " + lvl;
            int lvlWidth = mc.font.width(lvlText);
            graphics.text(mc.font, lvlText, startX + boxWidth - lvlWidth - 4, y + 4, white);

            // 4. Draw XP Amount (Middle - Yellow)
            String xpText = "+" + ticket.amount + "xp";
            graphics.text(mc.font, xpText, startX + 22, y + 13, yellow);

            // 5. Draw Progress Bar (Bottom - leaving 1px space from border)
            int barWidth = boxWidth - 26;
            int barX = startX + 22;
            int barY = y + 24; // Lowered to leave space
            
            // Background
            graphics.fill(barX, barY, barX + barWidth, barY + 3, gray);
            // Progress
            graphics.fill(barX, barY, barX + (int)(barWidth * progress), barY + 3, barGreen);
            // Outline
            graphics.outline(barX - 1, barY - 1, barWidth + 2, 5, (alpha << 24) | 0x000000);

            // Move Y up for the next notification in the stack (Box height + gap)
            y -= (boxHeight + 4);

            graphics.pose().popMatrix();
        }
    }
}