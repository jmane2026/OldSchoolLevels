package com.jmane2026.oldschoollevels.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public enum Skill {
    ATTACK("Attack", Items.IRON_SWORD, 0),
    DEFENSE("Defense", Items.IRON_CHESTPLATE, 0),
    STRENGTH("Strength", Items.TOTEM_OF_UNDYING, 0),
    RANGED("Ranged", Items.BOW, 0),
    LIFE("Life", Items.APPLE, 1154), // Level 10 XP
    MINING("Mining", Items.IRON_PICKAXE, 0),
    SMITHING("Smithing", Items.ANVIL, 0),
    WOODCUTTING("Wood Cutting", Items.OAK_LOG, 0),
    FLETCHING("Fletching", Items.ARROW, 0),
    FISHING("Fishing", Items.FISHING_ROD, 0),
    COOKING("Cooking", Items.BREAD, 0);

    private final String displayName;
    private final ItemStack icon;
    private final long defaultXp;

    Skill(String displayName, ItemLike item, long defaultXp) {
        this.displayName = displayName;
        this.icon = new ItemStack(item);
        this.defaultXp = defaultXp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Component getNameComponent() {
        return Component.literal(displayName);
    }

    public long getDefaultXp() {
        return defaultXp;
    }
}