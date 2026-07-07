package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.ArrayList;
import java.util.List;

public class XpNotificationOverlay {
    // Inner class to track individual floating XP text instances
    private static class XpDrop {
        final Skill skill;
        final long amount;
        final int stagger;
        int ticks;

        XpDrop(Skill skill, long amount, int stagger) {
            this.skill = skill;
            this.amount = amount;
            this.stagger = stagger;
            this.ticks = MAX_TICKS;
        }
    }

    private static final List<XpDrop> ACTIVE_DROPS = new ArrayList<>();
    private static Skill activeSkill = null;
    private static long currentTotalXp = 0;
    private static int boxTicks = 0;
    private static final int MAX_TICKS = 80; // 4 seconds

    public static void notify(Skill skill, long amount, long total) {
        // Calculate levels to detect a level-up
        int oldLevel = ExperienceUtils.getLevelAtExperience(total - amount);
        int newLevel = ExperienceUtils.getLevelAtExperience(total);

        if (newLevel > oldLevel) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                // Play the firework blast sound at the player's location
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.0f, false);
            }
        }

        // Calculate stagger based on how many drops were added in the last few ticks
        int stagger = 0;
        for (XpDrop drop : ACTIVE_DROPS) {
            if (drop.ticks > MAX_TICKS - 5) stagger++;
        }

        ACTIVE_DROPS.add(new XpDrop(skill, amount, stagger));

        // Update HUD box state to reflect the most recent gain
        activeSkill = skill;
        currentTotalXp = total;
        boxTicks = MAX_TICKS;
    }

    public static void clientTick() {
        if (Minecraft.getInstance().isPaused()) return;

        if (boxTicks > 0) boxTicks--;
        
        // Update all active drops and remove expired ones
        ACTIVE_DROPS.removeIf(drop -> {
            drop.ticks--;
            return drop.ticks <= 0;
        });
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if ((boxTicks <= 0 && ACTIVE_DROPS.isEmpty()) || activeSkill == null) return;

        Minecraft mc = Minecraft.getInstance();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = 10;
        int y = screenHeight - 75;
        int width = 120;
        int height = 35;

        // 1. Render the HUD Box (Background, Icon, Progress Bar)
        if (boxTicks > 0) {
            renderHudBox(graphics, mc, x, y, width, height, partialTick);
        }

        // 2. Render all active scrolling XP drops
        for (XpDrop drop : ACTIVE_DROPS) {
            renderXpDrop(graphics, mc, drop, x, y, partialTick);
        }
    }

    private static void renderHudBox(GuiGraphicsExtractor graphics, Minecraft mc, int x, int y, int width, int height, float partialTick) {
        // Calculate Alpha for fading
        int alpha = (boxTicks < 15) ? (int) (255 * ((boxTicks - partialTick) / 15f)) : 255;
        alpha = Math.max(0, Math.min(255, alpha));

        int bgColor = (alpha * 0xAA / 255) << 24;
        int textColor = (alpha << 24) | 0xFFFFFF;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.outline(x, y, width, height, (alpha << 24) | 0xFFFFFF);
        graphics.item(activeSkill.getIcon(), x + 5, y + 5);

        int level = ExperienceUtils.getLevelAtExperience(currentTotalXp);
        graphics.text(mc.font, activeSkill.getDisplayName() + " Lvl: " + level, x + 25, y + 7, textColor);

        long prevXp = ExperienceUtils.getExperienceAtLevel(level);
        long nextXp = ExperienceUtils.getExperienceAtLevel(level + 1);
        float progress = (float) (currentTotalXp - prevXp) / (float) (nextXp - prevXp);
        if (level >= 99) progress = 1.0f;

        int barWidth = width - 30;
        graphics.fill(x + 25, y + 20, x + 25 + barWidth, y + 24, (alpha << 24) | 0x444444);
        graphics.fill(x + 25, y + 20, x + 25 + (int) (barWidth * progress), y + 24, (alpha << 24) | 0x00FF00);
    }

    private static void renderXpDrop(GuiGraphicsExtractor graphics, Minecraft mc, XpDrop drop, int x, int y, float partialTick) {
        int alpha = (drop.ticks < 15) ? (int) (255 * ((drop.ticks - partialTick) / 15f)) : 255;
        alpha = Math.max(0, Math.min(255, alpha));
        int xpColor = (alpha << 24) | 0xFFFF00;

        // Scrolling XP Text
        float scrollProgress = 1.0f - (((float) drop.ticks - partialTick) / (float) MAX_TICKS);
        // Start 10 pixels above the box and stack upwards by subtracting the stagger
        float scrollY = (float) y - 10f - (scrollProgress * 50f) - (drop.stagger * 12f);

        graphics.pose().pushMatrix();
        graphics.pose().translate((float) x + 5f, scrollY, graphics.pose());

        // Render a small version of the skill icon next to the floating text
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.6f, 0.6f, graphics.pose());
        graphics.item(drop.skill.getIcon(), 0, 0);
        graphics.pose().popMatrix();

        // Render the XP text with a slight offset to the right of the small icon
        graphics.text(mc.font, "+" + drop.amount, 12, 2, xpColor);
        graphics.pose().popMatrix();
    }
}