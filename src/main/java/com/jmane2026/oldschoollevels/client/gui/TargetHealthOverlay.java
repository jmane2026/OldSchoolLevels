package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.OSLConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetHealthOverlay {
    // Cache to keep the bar visible
    private static String lastTargetName = "";
    private static float lastHealth = 0;
    private static float lastMaxHealth = 0;
    private static int displayTicks = 0;
    private static int lastEntityId = -1;

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (!OSLConfig.ENABLE_ENTITY_HEALTH_BARS.get()) return;

        if (mc.options.hideGui || mc.player == null || mc.level == null) return;

        // 1. Perform a long-range Raycast to find a target (20 blocks)
        double range = 20.0;
        Vec3 eyePos = mc.player.getEyePosition(partialTick);
        Vec3 lookVec = mc.player.getViewVector(partialTick);
        Vec3 reachVec = eyePos.add(lookVec.scale(range));
        AABB searchBox = mc.player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                mc.player, eyePos, reachVec, searchBox,
                (e) -> e instanceof LivingEntity && e.isAlive() && e != mc.player,
                range * range
        );

        // 2. Update Cache if looking at a valid entity
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            // Don't render for Bosses who already have vanilla boss bars
            if (target instanceof Giant || target.getType() == EntityType.WITHER || target.getType() == EntityType.ENDER_DRAGON) return;

            lastEntityId = target.getId();
            lastTargetName = target.getDisplayName().getString();
            lastHealth = target.getHealth();
            lastMaxHealth = target.getMaxHealth();
            displayTicks = 60; // Stay visible for 3 seconds (60 ticks)
        } else {
            // If not looking at anything, check if our current cached entity still exists but is dead
            Entity worldEntity = mc.level.getEntity(lastEntityId);
            if (worldEntity instanceof LivingEntity living && !living.isAlive()) {
                lastHealth = 0; // Force bar to 0 for the death delay
            }

            if (displayTicks > 0) {
                displayTicks--;
            }
        }

        // 3. Render if within the display window
        if (displayTicks > 0) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int x = (screenWidth / 2) - 60;
            int y = 20;

            // Calculate alpha: Stay at 100% until 20 ticks remain, then fade to 0%
            float alpha = displayTicks > 20 ? 1.0f : displayTicks / 20.0f;

            float ratio = Math.max(0, lastHealth / lastMaxHealth);

            // Background & Border
            graphics.fill(x - 2, y - 2, x + 122, y + 12, applyAlpha(0xAA000000, alpha));
            graphics.outline(x - 1, y - 1, 122, 12, applyAlpha(0xFF373737, alpha));

            // OSRS Red (Missing/Total)
            graphics.fill(x, y, x + 120, y + 10, applyAlpha(0xFFFF0000, alpha));

            // OSRS Green (Current)
            if (ratio > 0) {
                graphics.fill(x, y, x + (int)(120 * ratio), y + 10, applyAlpha(0xFF00FF00, alpha));
            }

            // Entity Name
            graphics.centeredText(mc.font, Component.literal(lastTargetName), screenWidth / 2, y - 10, applyAlpha(0xFFFFFFFF, alpha));

            // Numeric Health
            // Use ceiling so that 0.1 HP shows as 1 HP (prevents 0/3 while still alive)
            String healthText = (int)Math.ceil(lastHealth) + " / " + (int)Math.ceil(lastMaxHealth);
            graphics.centeredText(mc.font, Component.literal(healthText), screenWidth / 2, y + 1, applyAlpha(0xFFFFFFFF, alpha));
        }
    }

    /**
     * Utility to scale the alpha component of an ARGB color.
     */
    private static int applyAlpha(int color, float alpha) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int newA = (int)(a * alpha);
        return (newA << 24) | (r << 16) | (g << 8) | b;
    }
}