package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.items.KnifeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ArrowItem;
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
}