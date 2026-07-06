package com.jmane2026.oldschoollevels;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(OldSchoolLevels.MODID)
public class OldSchoolLevels {
    public static final String MODID = "oldschoollevels";

    public OldSchoolLevels(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
    }
}
