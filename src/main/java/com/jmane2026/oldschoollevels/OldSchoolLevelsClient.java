package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.client.gui.LevelScreen;
import com.jmane2026.oldschoollevels.client.gui.XpNotificationOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class OldSchoolLevelsClient {
    public static final KeyMapping LEVEL_SCREEN_KEY = new KeyMapping(
            "key.oldschoollevels.open_levels",
            GLFW.GLFW_KEY_O,
            KeyMapping.Category.MISC
    );

    public OldSchoolLevelsClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(LEVEL_SCREEN_KEY);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "xp_notifications"), (graphics, partialTick) -> {
            // partialTick is a DeltaTracker in 26.x, we need to extract the float value
            XpNotificationOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
        });
    }
}

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
class ClientGameEvents {
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        XpNotificationOverlay.clientTick();
        while (OldSchoolLevelsClient.LEVEL_SCREEN_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new LevelScreen(Component.literal("Skills")));
        }
    }
}
