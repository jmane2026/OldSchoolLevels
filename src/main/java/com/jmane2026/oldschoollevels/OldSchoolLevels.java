package com.jmane2026.oldschoollevels;

import com.jmane2026.oldschoollevels.common.OSLConfig;
import com.jmane2026.oldschoollevels.common.entities.GiantBossHandler;
import com.jmane2026.oldschoollevels.core.*;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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
        modEventBus.addListener(this::addCreative);

        container.registerConfig(ModConfig.Type.COMMON, OSLConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, OSLConfig.CLIENT_SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.OAK_BOW);
            event.accept(ModItems.ACACIA_BOW);
            event.accept(ModItems.BIRCH_BOW);
            event.accept(ModItems.CHERRY_BOW);
            event.accept(ModItems.DARK_OAK_BOW);
            event.accept(ModItems.JUNGLE_BOW);
            event.accept(ModItems.MANGROVE_BOW);
            event.accept(ModItems.SPRUCE_BOW);
            event.accept(ModItems.PALE_OAK_BOW);
            event.accept(ModItems.FLINT_ARROW);
            event.accept(ModItems.FLINT_KNIFE);
            event.accept(ModItems.COPPER_ARROW);
            event.accept(ModItems.COPPER_KNIFE);
            event.accept(ModItems.DIAMOND_ARROW);
            event.accept(ModItems.DIAMOND_KNIFE);
            event.accept(ModItems.EMERALD_ARROW);
            event.accept(ModItems.EMERALD_KNIFE);
            event.accept(ModItems.GOLDEN_ARROW);
            event.accept(ModItems.GOLDEN_KNIFE);
            event.accept(ModItems.IRON_ARROW);
            event.accept(ModItems.IRON_KNIFE);
            event.accept(ModItems.NETHERITE_ARROW);
            event.accept(ModItems.NETHERITE_KNIFE);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.AIR_ECHO);
            event.accept(ModItems.AIR_SIGIL);
            event.accept(ModItems.BLANK_SIGIL);
            event.accept(ModItems.COPPER_ARROW_HEADS);
            event.accept(ModItems.DIAMOND_ARROW_HEADS);
            event.accept(ModItems.EARTH_ECHO);
            event.accept(ModItems.EARTH_SIGIL);
            event.accept(ModItems.EMERALD_ARROW_HEADS);
            event.accept(ModItems.FIRE_ECHO);
            event.accept(ModItems.FIRE_SIGIL);
            event.accept(ModItems.FLINT_ARROW_HEADS);
            event.accept(ModItems.GOLDEN_ARROW_HEADS);
            event.accept(ModItems.IRON_ARROW_HEADS);
            event.accept(ModItems.LOGIC_ECHO);
            event.accept(ModItems.LOGIC_SIGIL);
            event.accept(ModItems.NETHERITE_ARROW_HEADS);
            event.accept(ModItems.RAW_SIGIL);
            event.accept(ModItems.SIGIL_POUCH);
            event.accept(ModItems.SIGILIC_ORE);
            event.accept(ModItems.WATER_ECHO);
            event.accept(ModItems.WATER_SIGIL);
        }
    }
}