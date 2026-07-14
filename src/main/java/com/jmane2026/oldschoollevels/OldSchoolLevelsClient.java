package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.client.gui.*;
import com.jmane2026.oldschoollevels.common.MagicHandler;
import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.core.ModEntities;
import com.jmane2026.oldschoollevels.core.ModMenus;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class OldSchoolLevelsClient {
    public OldSchoolLevelsClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "xp_notifications"), (graphics, partialTick) -> {
            // partialTick is a DeltaTracker in 26.x, we need to extract the float value
            XpNotificationOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
        });

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "damage_indicators"), (graphics, partialTick) -> {
            DamageIndicatorManager.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
        });

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "warning_hud"), (graphics, partialTick) -> {
            WarningOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
        });

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "echo_nav"), EchoNavigationOverlay::render);

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "active_spell"), MagicHandler::renderActiveSpell);

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "target_health"), (graphics, partialTick) -> {
            TargetHealthOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
        });

        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "stamina_bar"), (graphics, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            // Hide if F1 is pressed, player is null, or in Creative/Spectator (hasExperience() is false in those modes)
            if (mc.options.hideGui || mc.player == null || mc.gameMode == null || !mc.gameMode.hasExperience()) return;

            int x = mc.getWindow().getGuiScaledWidth() / 2 + 10;
            int y = mc.getWindow().getGuiScaledHeight() - 49; // Above food bar

            float stamina = mc.player.getData(ModAttachments.STAMINA.get());
            int level = ExperienceUtils.getLevelAtExperience(mc.player.getData(ModAttachments.SKILLS.get()).getExperience(Skill.MOBILITY));
            float maxStamina = RequirementUtils.getMaxStamina(level);
            float ratio = stamina / maxStamina;

            // Background (80px wide to align with vanilla bars)
            // Changed border to grayish color to match hotbar slots
            graphics.outline(x - 1, y - 1, 82, 7, 0xFF373737);
            graphics.fill(x, y, x + 80, y + 5, 0xAA000000);
            // Green Bar (drains from right to left)
            if (ratio > 0) {
                graphics.fill(x, y, x + (int)(80 * ratio), y + 5, 0xFF55FF55);
            }
        });
    }

    @SubscribeEvent
    static void onComputeFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Calculate the FOV shift caused by our Mobility speed bonus
        int level = ExperienceUtils.getLevelAtExperience(player.getData(ModAttachments.SKILLS.get()).getExperience(Skill.MOBILITY));
        float speedBonus = RequirementUtils.getMovementSpeedBonus(level);

        // Minecraft's FOV formula for speed is: ( (currentSpeed / walkingSpeed) + 1.0 ) / 2.0
        // We calculate the specific factor our Mobility skill contributes to that formula.
        float targetFactor = 1.0f + (speedBonus / 2.0f);

        // We must also account for the "FOV Effects" accessibility slider in the player's settings.
        float fovScale = Minecraft.getInstance().options.fovEffectScale().get().floatValue();
        float contribution = 1.0f + (targetFactor - 1.0f) * fovScale;

        // Divide the current modifier by our contribution to return the FOV to the user's baseline.
        event.setNewFovModifier(event.getFovModifier() / contribution);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SIGIL_POUCH_MENU.get(), SigilPouchScreen::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Link our custom projectile to the Wind Charge visual renderer
        event.registerEntityRenderer(ModEntities.AIR_BLAST_PROJECTILE.get(), WindChargeRenderer::new);
    }
}

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
class ClientGameEvents {
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        XpNotificationOverlay.clientTick();
        WarningOverlay.clientTick();
        DamageIndicatorManager.clientTick();

        // --- Momentum Preservation (Drag Reduction) ---
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        // Allow momentum retention in the air OR while skimming the surface of water (eyes dry)
        if (player != null && !player.onGround() && !player.getAbilities().flying && (!player.isInWater() || !player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER))) {
            // Check if we are actually moving horizontally
            if (player.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                int level = ExperienceUtils.getLevelAtExperience(player.getData(ModAttachments.SKILLS.get()).getExperience(Skill.MOBILITY));
                float retention = RequirementUtils.getMomentumRetention(level);
                
                if (retention > 1.0f) {
                    Vec3 d = player.getDeltaMovement();
                    player.setDeltaMovement(d.x * retention, d.y, d.z * retention);
                }
            }
        }
    }

    @SubscribeEvent
    static void onScreenRender(ScreenEvent.Render.Post event) {
        float pt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        WarningOverlay.render(event.getGuiGraphics(), pt);
    }
}