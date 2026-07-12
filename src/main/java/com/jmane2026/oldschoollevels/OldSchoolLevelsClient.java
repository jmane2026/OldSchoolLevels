package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.client.gui.*;
import com.jmane2026.oldschoollevels.common.MagicHandler;
import com.jmane2026.oldschoollevels.core.ModBlocks;
import com.jmane2026.oldschoollevels.core.ModEntities;
import com.jmane2026.oldschoollevels.core.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
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
    }

    @SubscribeEvent
    static void onScreenRender(ScreenEvent.Render.Post event) {
        float pt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        WarningOverlay.render(event.getGuiGraphics(), pt);
    }
}