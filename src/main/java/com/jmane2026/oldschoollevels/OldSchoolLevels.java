package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.core.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(OldSchoolLevels.MODID)
public class OldSchoolLevels {
    public static final String MODID = "oldschoollevels";

    public OldSchoolLevels(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }
}
