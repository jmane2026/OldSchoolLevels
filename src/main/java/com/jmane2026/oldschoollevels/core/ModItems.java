package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.items.KnifeItem;
import com.jmane2026.oldschoollevels.common.items.EchoItem;
import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OldSchoolLevels.MODID);

    // Knives
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.registerItem("flint_knife", KnifeItem::new, p -> p.durability(48).stacksTo(1));
    public static final DeferredItem<Item> COPPER_KNIFE = ITEMS.registerItem("copper_knife", KnifeItem::new, p -> p.durability(128).stacksTo(1));
    public static final DeferredItem<Item> IRON_KNIFE = ITEMS.registerItem("iron_knife", KnifeItem::new, p -> p.durability(250).stacksTo(1));
    public static final DeferredItem<Item> GOLDEN_KNIFE = ITEMS.registerItem("golden_knife", KnifeItem::new, p -> p.durability(32).stacksTo(1));
    public static final DeferredItem<Item> EMERALD_KNIFE = ITEMS.registerItem("emerald_knife", KnifeItem::new, p -> p.durability(1024).stacksTo(1));
    public static final DeferredItem<Item> DIAMOND_KNIFE = ITEMS.registerItem("diamond_knife", KnifeItem::new, p -> p.durability(1561).stacksTo(1));
    public static final DeferredItem<Item> NETHERITE_KNIFE = ITEMS.registerItem("netherite_knife", KnifeItem::new, p -> p.durability(2031).stacksTo(1).fireResistant());

    // Bows
    public static final DeferredItem<Item> OAK_BOW = ITEMS.registerItem("oak_bow", BowItem::new, p -> p.durability(384));
    public static final DeferredItem<Item> SPRUCE_BOW = ITEMS.registerItem("spruce_bow", BowItem::new, p -> p.durability(450));
    public static final DeferredItem<Item> BIRCH_BOW = ITEMS.registerItem("birch_bow", BowItem::new, p -> p.durability(450));
    public static final DeferredItem<Item> JUNGLE_BOW = ITEMS.registerItem("jungle_bow", BowItem::new, p -> p.durability(550));
    public static final DeferredItem<Item> ACACIA_BOW = ITEMS.registerItem("acacia_bow", BowItem::new, p -> p.durability(680));
    public static final DeferredItem<Item> DARK_OAK_BOW = ITEMS.registerItem("dark_oak_bow", BowItem::new, p -> p.durability(800));
    public static final DeferredItem<Item> MANGROVE_BOW = ITEMS.registerItem("mangrove_bow", BowItem::new, p -> p.durability(950));
    public static final DeferredItem<Item> CHERRY_BOW = ITEMS.registerItem("cherry_bow", BowItem::new, p -> p.durability(1100));

    // Arrows
    public static final DeferredItem<Item> FLINT_ARROW = ITEMS.registerItem("flint_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> COPPER_ARROW = ITEMS.registerItem("copper_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> IRON_ARROW = ITEMS.registerItem("iron_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> GOLDEN_ARROW = ITEMS.registerItem("golden_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> EMERALD_ARROW = ITEMS.registerItem("emerald_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> DIAMOND_ARROW = ITEMS.registerItem("diamond_arrow", ArrowItem::new, p -> p);
    public static final DeferredItem<Item> NETHERITE_ARROW = ITEMS.registerItem("netherite_arrow", ArrowItem::new, p -> p);

    // Arrow Heads
    public static final DeferredItem<Item> FLINT_ARROW_HEADS = ITEMS.registerItem("flint_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> COPPER_ARROW_HEADS = ITEMS.registerItem("copper_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> IRON_ARROW_HEADS = ITEMS.registerItem("iron_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> GOLDEN_ARROW_HEADS = ITEMS.registerItem("golden_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> EMERALD_ARROW_HEADS = ITEMS.registerItem("emerald_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> DIAMOND_ARROW_HEADS = ITEMS.registerItem("diamond_arrow_heads", Item::new, p -> p);
    public static final DeferredItem<Item> NETHERITE_ARROW_HEADS = ITEMS.registerItem("netherite_arrow_heads", Item::new, p -> p);

    // Arcana Materials
    public static final DeferredItem<Item> RAW_SIGIL = ITEMS.registerItem("raw_sigil", Item::new, p -> p);
    public static final DeferredItem<BlockItem> SIGILIC_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.SIGILIC_ORE);
    public static final DeferredItem<Item> BLANK_SIGIL = ITEMS.registerItem("blank_sigil", Item::new, p -> p);

    // Echoes (Passive Compasses)
    public static final DeferredItem<Item> AIR_ECHO = ITEMS.registerItem("air_echo", p -> new EchoItem(p, "air_dome"), p -> p.stacksTo(1));
    public static final DeferredItem<Item> WATER_ECHO = ITEMS.registerItem("water_echo", p -> new EchoItem(p, "water_dome"), p -> p.stacksTo(1));
    public static final DeferredItem<Item> EARTH_ECHO = ITEMS.registerItem("earth_echo", p -> new EchoItem(p, "earth_dome"), p -> p.stacksTo(1));
    public static final DeferredItem<Item> FIRE_ECHO = ITEMS.registerItem("fire_echo", p -> new EchoItem(p, "fire_dome"), p -> p.stacksTo(1));
    public static final DeferredItem<Item> LOGIC_ECHO = ITEMS.registerItem("logic_echo", p -> new EchoItem(p, "air_dome"), p -> p.stacksTo(1));

    // Sigils
    public static final DeferredItem<Item> AIR_SIGIL = ITEMS.registerItem("air_sigil", Item::new, p -> p);
    public static final DeferredItem<Item> WATER_SIGIL = ITEMS.registerItem("water_sigil", Item::new, p -> p);
    public static final DeferredItem<Item> EARTH_SIGIL = ITEMS.registerItem("earth_sigil", Item::new, p -> p);
    public static final DeferredItem<Item> FIRE_SIGIL = ITEMS.registerItem("fire_sigil", Item::new, p -> p);
    public static final DeferredItem<Item> LOGIC_SIGIL = ITEMS.registerItem("logic_sigil", Item::new, p -> p);

    public static final DeferredItem<SigilPouchItem> SIGIL_POUCH = ITEMS.registerItem("sigil_pouch", SigilPouchItem::new, p -> p.stacksTo(1).rarity(Rarity.EPIC));
}