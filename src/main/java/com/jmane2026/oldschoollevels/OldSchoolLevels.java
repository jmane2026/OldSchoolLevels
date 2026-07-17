package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.common.OSLConfig;
import com.jmane2026.oldschoollevels.common.entities.GiantBossHandler;
import com.jmane2026.oldschoollevels.core.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

@Mod(OldSchoolLevels.MODID)
public class OldSchoolLevels {
    public static final String MODID = "oldschoollevels";

    public OldSchoolLevels(IEventBus modEventBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        modEventBus.addListener(GiantBossHandler::onAttributeModification);
        ModMenus.MENUS.register(modEventBus);

        container.registerConfig(ModConfig.Type.COMMON, OSLConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, OSLConfig.CLIENT_SPEC);
    }
}