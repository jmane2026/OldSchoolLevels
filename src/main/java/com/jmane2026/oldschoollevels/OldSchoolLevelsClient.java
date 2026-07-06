package com.jmane2026.oldschoollevels;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = OldSchoolLevels.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class OldSchoolLevelsClient {
    public OldSchoolLevelsClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }
}
