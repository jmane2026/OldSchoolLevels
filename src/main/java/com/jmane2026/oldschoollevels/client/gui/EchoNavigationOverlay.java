package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.core.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.DeltaTracker;

public class EchoNavigationOverlay {
    private static final Identifier ARROW = Identifier.fromNamespaceAndPath("oldschoollevels", "textures/gui/nav_arrow.png");

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        // Get synced target from attachment
        BlockPos target = mc.player.getData(ModAttachments.ECHO_TARGET.get());
        if (target.equals(BlockPos.ZERO)) return;

        // Calculate Angle and Distance
        double dz = target.getZ() - mc.player.getZ();
        double dx = target.getX() - mc.player.getX();
        float angleToTarget = (float) (Mth.atan2(dz, dx) * (180 / Math.PI)) - 90.0F;
        float playerYaw = mc.player.getViewYRot(partialTick);
        float relativeAngle = Mth.wrapDegrees(angleToTarget - playerYaw);

        // Using getCenter() provides a Vec3 for the middle of the target block
        double distSq = mc.player.distanceToSqr(target.getCenter().x, mc.player.getY(), target.getCenter().z);
        int distance = (int) Math.sqrt(distSq);

        // Position Logic: 75 pixels from the bottom to clear the hotbar and XP bar
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int y = screenHeight - 90;

        // 2. Render Rotating Arrow
        graphics.pose().pushMatrix();

        graphics.pose().translate((float)centerX, (float)y, graphics.pose());
        graphics.pose().rotate((float) Math.toRadians(relativeAngle), graphics.pose());

        // Render Rotating Arrow (16x16 rendered size from a 32x32 texture)
        graphics.blit(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, ARROW, -8, -8, 0.0f, 0.0f, 16, 16, 32, 32, 32, 32, -1);

        graphics.pose().popMatrix();

        // Render Distance Text below the arrow
        String distStr = distance + "m";
        graphics.centeredText(mc.font, distStr, centerX, y + 12, 0xFFFFFFFF);
    }
}