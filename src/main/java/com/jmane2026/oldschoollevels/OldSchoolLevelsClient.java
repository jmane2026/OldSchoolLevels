package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.client.gui.LevelScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
}

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
class ClientGameEvents {
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        while (OldSchoolLevelsClient.LEVEL_SCREEN_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(new LevelScreen(Component.literal("Skills")));
        }
    }
}
