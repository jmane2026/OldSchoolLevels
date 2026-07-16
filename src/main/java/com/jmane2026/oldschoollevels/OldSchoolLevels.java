package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.common.entities.GiantBossHandler;
import com.jmane2026.oldschoollevels.core.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(OldSchoolLevels.MODID)
public class OldSchoolLevels {
    public static final String MODID = "oldschoollevels";

    public OldSchoolLevels(IEventBus modEventBus, ModContainer ignoredModContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        modEventBus.addListener(GiantBossHandler::onAttributeModification);
        ModMenus.MENUS.register(modEventBus);
    }
}