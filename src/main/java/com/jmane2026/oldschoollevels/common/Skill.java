package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.core.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public enum Skill {
    ATTACK("Attack", () -> Items.IRON_SWORD, 0),
    DEFENSE("Defense", () -> Items.IRON_CHESTPLATE, 0),
    STRENGTH("Strength", () -> Items.TOTEM_OF_UNDYING, 0),
    RANGED("Ranged", () -> Items.BOW, 0),
    LIFE("Life", () -> Items.APPLE, 1154, Identifier.withDefaultNamespace("hud/heart/full")), // Level 10 XP
    MINING("Mining", () -> Items.IRON_PICKAXE, 0),
    SMITHING("Smithing", () -> Items.ANVIL, 0),
    WOODCUTTING("Wood Cutting", () -> Items.OAK_LOG, 0),
    FLETCHING("Fletching", ModItems.FLINT_ARROW, 0),
    FISHING("Fishing", () -> Items.FISHING_ROD, 0),
    COOKING("Cooking", () -> Items.BREAD, 0),
    ARCANA("Arcana", ModItems.BLANK_SIGIL, 0),
    MAGIC("Magic", ModItems.AIR_SIGIL, 0, Identifier.withDefaultNamespace("textures/block/fire_0.png")),
    MOBILITY("Mobility", () -> Items.LEATHER_BOOTS, 0);

    private final String displayName;
    private final Supplier<Item> itemSupplier;
    private final long defaultXp;
    private final Identifier spriteIcon;

    Skill(String displayName, Supplier<Item> itemSupplier, long defaultXp) {
        this(displayName, itemSupplier, defaultXp, null);
    }

    Skill(String displayName, Supplier<Item> itemSupplier, long defaultXp, Identifier spriteIcon) {
        this.displayName = displayName;
        this.itemSupplier = itemSupplier;
        this.defaultXp = defaultXp;
        this.spriteIcon = spriteIcon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getIcon() {
        return new ItemStack(this.itemSupplier.get());
    }

    public Identifier getSpriteIcon() {
        return spriteIcon;
    }

    public long getDefaultXp() {
        return defaultXp;
    }
}