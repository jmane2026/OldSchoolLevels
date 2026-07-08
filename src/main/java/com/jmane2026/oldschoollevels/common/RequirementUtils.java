package com.jmane2026.oldschoollevels.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class RequirementUtils {

    public static int getRequiredMiningLevel(Block block) {
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) return 1;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 1;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 5;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 15;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 30;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE) return 40;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 65;
        if (block == Blocks.ANCIENT_DEBRIS) return 85;
        return 1;
    }

    public static int getRequiredWoodcuttingLevel(Block block) {
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_WOOD) return 1;
        if (block == Blocks.SPRUCE_LOG || block == Blocks.SPRUCE_WOOD || block == Blocks.BIRCH_LOG || block == Blocks.BIRCH_WOOD) return 15;
        if (block == Blocks.JUNGLE_LOG || block == Blocks.JUNGLE_WOOD) return 30;
        if (block == Blocks.ACACIA_LOG || block == Blocks.ACACIA_WOOD) return 45;
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.DARK_OAK_WOOD) return 60;
        if (block == Blocks.MANGROVE_LOG || block == Blocks.MANGROVE_WOOD) return 70;
        if (block == Blocks.CHERRY_LOG || block == Blocks.CHERRY_WOOD) return 85;
        return 1;
    }

    public static int getRequiredDefenseLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!isArmor(path)) return 1;

        if (path.contains("copper")) return 5;
        if (path.contains("iron")) return 15;
        if (path.contains("gold")) return 40;
        if (path.contains("diamond")) return 65;
        if (path.contains("netherite")) return 85;
        return 1;
    }

    public static int getRequiredAttackLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!isTool(path) && !path.contains("sword")) return 1;

        if (path.contains("copper")) return 5;
        if (path.contains("iron")) return 15;
        if (path.contains("gold")) return 40;
        if (path.contains("diamond")) return 65;
        if (path.contains("netherite")) return 85;
        return 1;
    }

    public static int getRequiredSmithingLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // Check both the Result (Ingots) and the Inputs (Raw Ores)
        if (stack.is(Items.RAW_COPPER) || stack.is(Items.COPPER_INGOT)) return 5;
        if (stack.is(Items.RAW_IRON) || stack.is(Items.IRON_INGOT)) return 15;
        if (stack.is(Items.RAW_GOLD) || stack.is(Items.GOLD_INGOT)) return 40;
        if (stack.is(Items.NETHERITE_SCRAP)) return 85;

        // Handle Ores that smelt into items (like Iron Ore block)
        if (path.contains("iron_ore")) return 15;
        if (path.contains("gold_ore")) return 40;

        if (path.contains("copper")) return 5;
        if (path.contains("iron")) return 15;
        if (path.contains("gold")) return 40;
        if (path.contains("diamond")) return 65;
        if (path.contains("netherite")) return 85;
        return 1;
    }

    public static int getRequiredRangedLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // Bow Tiers
        if (path.contains("oak_bow")) return 1;
        if (path.contains("spruce_bow") || path.contains("birch_bow")) return 15;
        if (path.contains("jungle_bow")) return 30;
        if (path.contains("acacia_bow")) return 45;
        if (path.contains("dark_oak_bow")) return 60;
        if (path.contains("mangrove_bow")) return 75;
        if (path.contains("cherry_bow")) return 85;

        // Arrow Tiers
        if (path.contains("copper_arrow")) return 5;
        if (path.contains("iron_arrow")) return 15;
        if (path.contains("golden_arrow")) return 40;
        if (path.contains("diamond_arrow")) return 65;
        if (path.contains("netherite_arrow")) return 85;

        return 1;
    }

    public static int getRequiredCookingLevel(ItemStack stack) {
        // Check both Inputs and Results
        if (stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF) ||
                stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP) ||
                stack.is(Items.RABBIT) || stack.is(Items.COOKED_RABBIT) || stack.is(Items.BREAD)) return 1;
        if (stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON) || stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN)) return 25;
        if (stack.is(Items.MUSHROOM_STEW) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON)) return 40;
        if (stack.is(Items.CAKE) || stack.is(Items.RABBIT_STEW) || stack.is(Items.COD) || stack.is(Items.COOKED_COD)) return 55;
        return 1;
    }

    private static boolean isArmor(String path) {
        return path.contains("helmet") || path.contains("chestplate") || path.contains("leggings") || path.contains("boots");
    }

    private static boolean isTool(String path) {
        return path.contains("pickaxe") || path.contains("axe") || path.contains("shovel") || path.contains("hoe");
    }

    public static int getRequiredFishingLevel(ItemStack stack) {
        if (stack.is(Items.COD)) return 1;
        if (stack.is(Items.SALMON)) return 25;
        if (stack.is(Items.TROPICAL_FISH)) return 50;
        if (stack.is(Items.PUFFERFISH)) return 70;
        return 1;
    }

    public record UnlockInfo(int level, String description, ItemStack icon) {}

    public static List<UnlockInfo> getUnlocksForSkill(Skill skill) {
        List<UnlockInfo> unlocks = new ArrayList<>();
        switch (skill) {
            case MINING -> populateMiningUnlocks(unlocks);
            case WOODCUTTING -> populateWoodcuttingUnlocks(unlocks);
            case FISHING -> populateFishingUnlocks(unlocks);
            case ATTACK -> populateAttackUnlocks(unlocks);
            case DEFENSE -> populateDefenseUnlocks(unlocks);
            case SMITHING -> populateSmithingUnlocks(unlocks);
            case COOKING -> populateCookingUnlocks(unlocks);
            case FLETCHING -> populateFletchingUnlocks(unlocks);
            case RANGED -> populateRangedUnlocks(unlocks);
            case LIFE -> {
                unlocks.add(new UnlockInfo(11, "Health Bonus (+1 HP)", new ItemStack(Items.APPLE)));
                unlocks.add(new UnlockInfo(13, "Health Bonus (+2 HP)", new ItemStack(Items.GOLDEN_APPLE)));
                unlocks.add(new UnlockInfo(15, "Health Bonus (+3 HP)", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
                unlocks.add(new UnlockInfo(20, "Health Bonus (+5 HP)", new ItemStack(Items.GLISTERING_MELON_SLICE)));
            }
        }
        return unlocks;
    }

    private static void populateFletchingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Oak Bows", new ItemStack(Items.BOW)));
        unlocks.add(new UnlockInfo(1, "Flint Arrows", new ItemStack(Items.ARROW)));
        unlocks.add(new UnlockInfo(5, "Copper Arrows", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Spruce Bows", new ItemStack(Blocks.SPRUCE_LOG)));
        unlocks.add(new UnlockInfo(15, "Birch Bows", new ItemStack(Blocks.BIRCH_LOG)));
        unlocks.add(new UnlockInfo(15, "Iron Arrows", new ItemStack(Items.IRON_INGOT)));
        unlocks.add(new UnlockInfo(30, "Jungle Bows", new ItemStack(Blocks.JUNGLE_LOG)));
        unlocks.add(new UnlockInfo(40, "Golden Arrows", new ItemStack(Items.GOLD_INGOT)));
        unlocks.add(new UnlockInfo(40, "Golden Arrows", new ItemStack(Items.GOLD_INGOT)));
        unlocks.add(new UnlockInfo(45, "Acacia Bows", new ItemStack(Blocks.ACACIA_LOG)));
        unlocks.add(new UnlockInfo(60, "Dark Oak Bows", new ItemStack(Blocks.DARK_OAK_LOG)));
        unlocks.add(new UnlockInfo(65, "Diamond Arrows", new ItemStack(Items.DIAMOND)));
        unlocks.add(new UnlockInfo(75, "Mangrove Bows", new ItemStack(Blocks.MANGROVE_LOG)));
        unlocks.add(new UnlockInfo(85, "Cherry Bows", new ItemStack(Blocks.CHERRY_LOG)));
        unlocks.add(new UnlockInfo(85, "Netherite Arrows", new ItemStack(Items.NETHERITE_INGOT)));
    }

    private static void populateRangedUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Oak Bows", new ItemStack(Items.BOW)));
        unlocks.add(new UnlockInfo(1, "Flint Arrows", new ItemStack(Items.ARROW)));
        unlocks.add(new UnlockInfo(5, "Copper Arrows", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Spruce Bows", new ItemStack(Blocks.SPRUCE_LOG)));
        unlocks.add(new UnlockInfo(15, "Birch Bows", new ItemStack(Blocks.BIRCH_LOG)));
        unlocks.add(new UnlockInfo(15, "Iron Arrows", new ItemStack(Items.IRON_INGOT)));
        unlocks.add(new UnlockInfo(30, "Jungle Bows", new ItemStack(Blocks.JUNGLE_LOG)));
        unlocks.add(new UnlockInfo(40, "Golden Arrows", new ItemStack(Items.GOLD_INGOT)));
        unlocks.add(new UnlockInfo(45, "Acacia Bows", new ItemStack(Blocks.ACACIA_LOG)));
        unlocks.add(new UnlockInfo(60, "Dark Oak Bows", new ItemStack(Blocks.DARK_OAK_LOG)));
        unlocks.add(new UnlockInfo(65, "Diamond Arrows", new ItemStack(Items.DIAMOND)));
        unlocks.add(new UnlockInfo(75, "Mangrove Bows", new ItemStack(Blocks.MANGROVE_LOG)));
        unlocks.add(new UnlockInfo(85, "Cherry Bows", new ItemStack(Blocks.CHERRY_LOG)));
        unlocks.add(new UnlockInfo(85, "Netherite Arrows", new ItemStack(Items.NETHERITE_INGOT)));
    }

    // Move existing case logic to private helpers to keep the switch clean
    private static void populateMiningUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Stone & Coal", new ItemStack(Items.COAL)));
        unlocks.add(new UnlockInfo(5, "Copper Ores", new ItemStack(Items.RAW_COPPER)));
        unlocks.add(new UnlockInfo(15, "Iron Ores", new ItemStack(Items.RAW_IRON)));
        unlocks.add(new UnlockInfo(30, "Lapis & Redstone", new ItemStack(Items.REDSTONE)));
        unlocks.add(new UnlockInfo(40, "Gold & Quartz", new ItemStack(Items.RAW_GOLD)));
        unlocks.add(new UnlockInfo(65, "Diamond & Emerald", new ItemStack(Items.DIAMOND)));
        unlocks.add(new UnlockInfo(85, "Ancient Debris", new ItemStack(Items.ANCIENT_DEBRIS)));
    }

    private static void populateWoodcuttingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Oak Trees", new ItemStack(Blocks.OAK_LOG)));
        unlocks.add(new UnlockInfo(15, "Spruce & Birch", new ItemStack(Blocks.SPRUCE_LOG)));
        unlocks.add(new UnlockInfo(30, "Jungle Trees", new ItemStack(Blocks.JUNGLE_LOG)));
        unlocks.add(new UnlockInfo(45, "Acacia Trees", new ItemStack(Blocks.ACACIA_LOG)));
        unlocks.add(new UnlockInfo(60, "Dark Oak Trees", new ItemStack(Blocks.DARK_OAK_LOG)));
        unlocks.add(new UnlockInfo(75, "Mangrove Trees", new ItemStack(Blocks.MANGROVE_LOG)));
        unlocks.add(new UnlockInfo(85, "Cherry Trees", new ItemStack(Blocks.CHERRY_LOG)));
    }

    private static void populateFishingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Raw Cod", new ItemStack(Items.COD)));
        unlocks.add(new UnlockInfo(25, "Raw Salmon", new ItemStack(Items.SALMON)));
        unlocks.add(new UnlockInfo(50, "Tropical Fish", new ItemStack(Items.TROPICAL_FISH)));
        unlocks.add(new UnlockInfo(70, "Pufferfish", new ItemStack(Items.PUFFERFISH)));
        unlocks.add(new UnlockInfo(99, "Mastery (No Junk)", new ItemStack(Items.FISHING_ROD)));
    }

    private static void populateAttackUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(5, "Copper Weapons/Tools", new ItemStack(Items.STONE_SWORD)));
        unlocks.add(new UnlockInfo(15, "Iron Weapons/Tools", new ItemStack(Items.IRON_SWORD)));
        unlocks.add(new UnlockInfo(40, "Gold Weapons/Tools", new ItemStack(Items.GOLDEN_SWORD)));
        unlocks.add(new UnlockInfo(65, "Diamond Weapons/Tools", new ItemStack(Items.DIAMOND_SWORD)));
        unlocks.add(new UnlockInfo(85, "Netherite Weapons/Tools", new ItemStack(Items.NETHERITE_SWORD)));
    }

    private static void populateDefenseUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(5, "Copper Armor", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Iron Armor", new ItemStack(Items.IRON_CHESTPLATE)));
        unlocks.add(new UnlockInfo(40, "Gold Armor", new ItemStack(Items.GOLDEN_CHESTPLATE)));
        unlocks.add(new UnlockInfo(65, "Diamond Armor", new ItemStack(Items.DIAMOND_CHESTPLATE)));
        unlocks.add(new UnlockInfo(85, "Netherite Armor", new ItemStack(Items.NETHERITE_CHESTPLATE)));
    }

    private static void populateSmithingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(5, "Copper Smelting/Craft", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Iron Smelting/Craft", new ItemStack(Items.IRON_INGOT)));
        unlocks.add(new UnlockInfo(40, "Gold Smelting/Craft", new ItemStack(Items.GOLD_INGOT)));
        unlocks.add(new UnlockInfo(85, "Netherite Smithing", new ItemStack(Items.NETHERITE_SCRAP)));
    }

    private static void populateCookingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Beef, Pork & Mutton", new ItemStack(Items.COOKED_BEEF)));
        unlocks.add(new UnlockInfo(25, "Salmon & Chicken", new ItemStack(Items.COOKED_SALMON)));
        unlocks.add(new UnlockInfo(40, "Pie & Stews", new ItemStack(Items.PUMPKIN_PIE)));
        unlocks.add(new UnlockInfo(55, "Cake & Rabbit Stew", new ItemStack(Items.CAKE)));
    }
}