package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.core.ModBlocks;
import com.jmane2026.oldschoollevels.core.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class RequirementUtils {

    public static int getRequiredMiningLevel(Block block) {
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) return 1;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE || block == ModBlocks.SIGILIC_ORE.get()) return 1;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 5;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 15;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 30;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE) return 40;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 65;
        if (block == Blocks.ANCIENT_DEBRIS) return 85;
        return 1;
    }

    public static int getRequiredWoodcuttingLevel(Block block) {
        if (block == Blocks.SPRUCE_LOG || block == Blocks.SPRUCE_WOOD || block == Blocks.BIRCH_LOG || block == Blocks.BIRCH_WOOD) return 15;
        if (block == Blocks.JUNGLE_LOG || block == Blocks.JUNGLE_WOOD) return 30;
        if (block == Blocks.ACACIA_LOG || block == Blocks.ACACIA_WOOD) return 45;
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.DARK_OAK_WOOD) return 60;
        if (block == Blocks.MANGROVE_LOG || block == Blocks.MANGROVE_WOOD) return 70;
        if (block == Blocks.CHERRY_LOG || block == Blocks.CHERRY_WOOD || block == Blocks.PALE_OAK_LOG || block == Blocks.PALE_OAK_WOOD) return 85;
        return 1;
    }

    public static int getRequiredDefenseLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!isArmor(path)) return 1;

        if (path.contains("leather")) return 1;
        if (path.contains("copper")) return 5;
        if (path.contains("chainmail")) return 15;
        if (path.contains("iron")) return 30;
        if (path.contains("gold")) return 40;
        if (path.contains("diamond")) return 65;
        if (path.contains("netherite")) return 85;
        return 1;
    }

    public static int getRequiredAttackLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!isTool(path) && !path.contains("sword") && !path.contains("_knife") && !path.contains("spear")) return 1;

        if (path.contains("copper")) return 5;
        if (path.contains("iron")) return 30;
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

        // Handle Ores that smelt into item (like Iron Ore block)
        if (path.contains("iron_ore")) return 15;
        if (path.contains("gold_ore")) return 40;

        if (path.contains("copper")) return 5;
        if (path.contains("chainmail")) return 15;
        if (path.contains("iron")) return 30;
        if (path.contains("gold")) return 40;
        if (path.contains("diamond")) return 65;
        if (path.contains("netherite")) return 85;
        return 1;
    }

    public static int getRequiredArcanaLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // Echoes are level 1 to ensure the skill is accessible
        if (path.contains("_echo")) {
            return 1;
        }

        // Sigils (Rune equivalents) - Conversion level
        if (path.contains("_sigil")) {
            if (path.contains("air")) return 1;
            if (path.contains("mind")) return 5;
            if (path.contains("water")) return 10;
            if (path.contains("earth")) return 20;
            if (path.contains("logic")) return 45;
            if (path.contains("fire")) return 35;
        }
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
        if (path.contains("flint_arrow")) return 1;
        if (path.contains("copper_arrow")) return 5;
        if (path.contains("iron_arrow")) return 15;
        if (path.contains("golden_arrow")) return 40;
        if (path.contains("diamond_arrow")) return 55;
        if (path.contains("emerald_arrow")) return 65;
        if (path.contains("netherite_arrow")) return 85;

        return 1;
    }

    public static int getRequiredFletchingLevel(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // Knives & Heads
        if (path.contains("flint")) return 1;
        if (path.contains("copper")) return 5;
        if (path.contains("iron")) return 15;
        if (path.contains("golden")) return 40;
        if (path.contains("diamond")) return 55;
        if (path.contains("emerald")) return 65;
        if (path.contains("netherite")) return 85;

        // Bows (Log Tiers)
        if (path.contains("oak")) return 1;
        if (path.contains("spruce") || path.contains("birch")) return 15;
        if (path.contains("jungle")) return 30;
        if (path.contains("acacia")) return 45;
        if (path.contains("dark_oak")) return 60;
        if (path.contains("mangrove")) return 75;
        if (path.contains("cherry")) return 85;

        return 1;
    }

    public static float getDefenseArmorBonus(int level) {
        return (level - 1) * 0.21f; // ~20.5 Armor points at Level 99
    }

    public static float getDefenseToughnessBonus(int level) {
        return (level - 1) * 0.125f; // ~12.2 Toughness points at Level 99 (Netherite Tier)
    }

    public static float getMovementSpeedBonus(int level) {
        if (!OSLConfig.ENABLE_MOVEMENT_SPEED_SCALING.get()) return 0.0f;
        return (level - 1) * 0.01f; // 1% faster per level
    }

    public static float getSwimSpeedBonus(int level) {
        if (!OSLConfig.ENABLE_SWIM_SPEED_SCALING.get()) return 0.0f;
        if (level < 45) return 0.0f;
        // Scaled to fit 0.0 - 1.0 range of WATER_MOVEMENT_EFFICIENCY
        return Math.min(1.0f, (level - 45) * 0.02f); // 2% efficiency per level above 45
    }

    public static float getJumpBoost(int level) {
        if (!OSLConfig.ENABLE_JUMP_SCALING.get()) return 0.0f;
        if (level < 20) return 0.0f;
        return (level - 1) * 0.015f; // ~1.5% higher per level
    }

    public static float getFallReduction(int level) {
        if (level < 40) return 0.0f;
        return (level - 1) * 0.01f; // 1% reduction per level
    }

    public static float getMaxStamina(int level) {
        return 100.0f + ((level - 1) * 2.0f); // +2 Max Stamina per level
    }

    public static float getMomentumRetention(int level) {
        // Vanilla air friction is roughly 0.91. 
        // At Level 99, we provide a factor to counteract this decay.
        return 1.0f + (level / 99.0f * 0.08f); 
    }

    public static float getArrowDamage(String path) {
        if (path.contains("netherite")) return 5.0f;
        if (path.contains("emerald")) return 4.0f;
        if (path.contains("diamond")) return 3.5f;
        if (path.contains("golden")) return 2.5f;
        if (path.contains("iron")) return 2.0f;
        if (path.contains("copper")) return 1.5f;
        return 1.0f; // Flint/Standard
    }

    public static float getBowDamageBonus(String path) {
        if (path.contains("cherry")) return 3.0f;
        if (path.contains("mangrove")) return 2.5f;
        if (path.contains("dark_oak")) return 2.0f;
        if (path.contains("acacia")) return 1.5f;
        if (path.contains("jungle")) return 1.0f;
        if (path.contains("spruce") || path.contains("birch")) return 0.5f;
        return 0.0f; // Oak (Uses vanilla baseline)
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

    public static float getKnifeDamage(String path) {
        if (path.contains("netherite")) return 5.0f;
        if (path.contains("diamond")) return 4.0f;
        if (path.contains("emerald")) return 3.5f;
        if (path.contains("golden")) return 1.5f;
        if (path.contains("iron")) return 3.0f;
        if (path.contains("copper")) return 2.5f;
        if (path.contains("flint")) return 2.0f;
        return 1.0f;
    }

    public static float getKnifeSpeed(String path) {
        if (path.contains("netherite")) return 3.0f;
        if (path.contains("diamond")) return 2.8f;
        if (path.contains("emerald")) return 2.8f;
        if (path.contains("golden")) return 3.2f;
        if (path.contains("iron")) return 2.4f;
        if (path.contains("copper")) return 2.2f;
        return 2.0f;
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
            case FLETCHING, RANGED -> populateArcheryUnlocks(unlocks);
            case ARCANA -> populateArcanaUnlocks(unlocks);
            case MAGIC -> populateMagicUnlocks(unlocks);
            case MOBILITY -> populateMobilityUnlocks(unlocks);
        }
        return unlocks;
    }

    private static void populateArcheryUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Oak Bows", new ItemStack((ItemLike) ModItems.OAK_BOW)));
        unlocks.add(new UnlockInfo(1, "Flint Arrows", new ItemStack((ItemLike) ModItems.FLINT_ARROW)));
        unlocks.add(new UnlockInfo(1, "Flint Arrow Heads", new ItemStack((ItemLike) ModItems.FLINT_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(5, "Copper Arrows", new ItemStack((ItemLike) ModItems.COPPER_ARROW)));
        unlocks.add(new UnlockInfo(5, "Copper Arrow Heads", new ItemStack((ItemLike) ModItems.COPPER_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(15, "Spruce Bows", new ItemStack((ItemLike) ModItems.SPRUCE_BOW)));
        unlocks.add(new UnlockInfo(15, "Birch Bows", new ItemStack((ItemLike) ModItems.BIRCH_BOW)));
        unlocks.add(new UnlockInfo(15, "Iron Arrows", new ItemStack((ItemLike) ModItems.IRON_ARROW)));
        unlocks.add(new UnlockInfo(15, "Iron Arrow Heads", new ItemStack((ItemLike) ModItems.IRON_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(30, "Jungle Bows", new ItemStack((ItemLike) ModItems.JUNGLE_BOW)));
        unlocks.add(new UnlockInfo(40, "Golden Arrows", new ItemStack((ItemLike) ModItems.GOLDEN_ARROW)));
        unlocks.add(new UnlockInfo(40, "Golden Arrow Heads", new ItemStack((ItemLike) ModItems.GOLDEN_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(45, "Acacia Bows", new ItemStack((ItemLike) ModItems.ACACIA_BOW)));
        unlocks.add(new UnlockInfo(55, "Diamond Arrows", new ItemStack((ItemLike) ModItems.DIAMOND_ARROW)));
        unlocks.add(new UnlockInfo(55, "Diamond Arrow Heads", new ItemStack((ItemLike) ModItems.DIAMOND_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(60, "Dark Oak Bows", new ItemStack((ItemLike) ModItems.DARK_OAK_BOW)));
        unlocks.add(new UnlockInfo(65, "Emerald Arrows", new ItemStack((ItemLike) ModItems.EMERALD_ARROW)));
        unlocks.add(new UnlockInfo(65, "Emerald Arrow Heads", new ItemStack((ItemLike) ModItems.EMERALD_ARROW_HEADS)));
        unlocks.add(new UnlockInfo(75, "Mangrove Bows", new ItemStack((ItemLike) ModItems.MANGROVE_BOW)));
        unlocks.add(new UnlockInfo(85, "Cherry Bows", new ItemStack((ItemLike) ModItems.CHERRY_BOW)));
        unlocks.add(new UnlockInfo(85, "Netherite Arrows", new ItemStack((ItemLike) ModItems.NETHERITE_ARROW)));
        unlocks.add(new UnlockInfo(85, "Netherite Arrow Heads", new ItemStack((ItemLike) ModItems.NETHERITE_ARROW_HEADS)));
    }

    private static void populateArcanaUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Air Sigils", new ItemStack(ModItems.AIR_SIGIL.get())));
        unlocks.add(new UnlockInfo(1, "Echoes", new ItemStack(ModItems.AIR_ECHO.get())));
        unlocks.add(new UnlockInfo(10, "Water Sigils", new ItemStack(ModItems.WATER_SIGIL.get())));
        unlocks.add(new UnlockInfo(20, "Earth Sigils", new ItemStack(ModItems.EARTH_SIGIL.get())));
        unlocks.add(new UnlockInfo(35, "Fire Sigils", new ItemStack(ModItems.FIRE_SIGIL.get())));
        unlocks.add(new UnlockInfo(45, "Logic Sigils", new ItemStack(ModItems.LOGIC_SIGIL.get())));
    }

    private static void populateMagicUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Air Blast", new ItemStack(ModItems.AIR_SIGIL.get())));
        unlocks.add(new UnlockInfo(10, "Blink", new ItemStack(Items.ENDER_EYE)));
        unlocks.add(new UnlockInfo(15, "Transmute Copper", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(20, "Spawn Teleport", new ItemStack(Items.CHORUS_FRUIT)));
        unlocks.add(new UnlockInfo(25, "Strength", new ItemStack(Items.BLAZE_POWDER)));
        unlocks.add(new UnlockInfo(30, "Transmute Iron", new ItemStack(Items.IRON_INGOT)));
        unlocks.add(new UnlockInfo(40, "Water Blast", new ItemStack(ModItems.WATER_SIGIL.get())));
        unlocks.add(new UnlockInfo(50, "Teleport", new ItemStack(Items.ENDER_PEARL)));
        unlocks.add(new UnlockInfo(55, "Telekinesis", new ItemStack(Items.ENDER_PEARL)));
        unlocks.add(new UnlockInfo(70, "Transmute Diamond", new ItemStack(Items.DIAMOND)));
        unlocks.add(new UnlockInfo(80, "Fire Blast", new ItemStack(ModItems.FIRE_SIGIL.get())));
        unlocks.add(new UnlockInfo(90, "Portal", new ItemStack(Items.ENDER_EYE)));
    }

    // Move existing case logic to private helpers to keep the switch clean
    private static void populateMiningUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Stone & Coal", new ItemStack(Items.COAL)));
        unlocks.add(new UnlockInfo(1, "Sigilic Ore", new ItemStack(ModItems.SIGILIC_ORE.get())));
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
        unlocks.add(new UnlockInfo(85, "Cherry & Pale Oak", new ItemStack(Blocks.PALE_OAK_LOG)));
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
        unlocks.add(new UnlockInfo(30, "Iron Weapons/Tools", new ItemStack(Items.IRON_SWORD)));
        unlocks.add(new UnlockInfo(40, "Gold Weapons/Tools", new ItemStack(Items.GOLDEN_SWORD)));
        unlocks.add(new UnlockInfo(65, "Diamond Weapons/Tools", new ItemStack(Items.DIAMOND_SWORD)));
        unlocks.add(new UnlockInfo(85, "Netherite Weapons/Tools", new ItemStack(Items.NETHERITE_SWORD)));
    }

    private static void populateDefenseUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Leather Armor", new ItemStack(Items.LEATHER_CHESTPLATE)));
        unlocks.add(new UnlockInfo(5, "Copper Armor", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Chainmail Armor", new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
        unlocks.add(new UnlockInfo(30, "Iron Armor", new ItemStack(Items.IRON_CHESTPLATE)));
        unlocks.add(new UnlockInfo(40, "Gold Armor", new ItemStack(Items.GOLDEN_CHESTPLATE)));
        unlocks.add(new UnlockInfo(65, "Diamond Armor", new ItemStack(Items.DIAMOND_CHESTPLATE)));
        unlocks.add(new UnlockInfo(85, "Netherite Armor", new ItemStack(Items.NETHERITE_CHESTPLATE)));
    }

    private static void populateSmithingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Blank Sigils", new ItemStack(ModItems.BLANK_SIGIL.get())));
        unlocks.add(new UnlockInfo(5, "Copper Smelting/Smithing", new ItemStack(Items.COPPER_INGOT)));
        unlocks.add(new UnlockInfo(15, "Iron Smelting", new ItemStack(Items.IRON_INGOT)));
        unlocks.add(new UnlockInfo(15, "Chainmail Smithing", new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
        unlocks.add(new UnlockInfo(30, "Iron Smithing", new ItemStack(Items.IRON_CHESTPLATE)));
        unlocks.add(new UnlockInfo(40, "Gold Smelting/Smithing", new ItemStack(Items.GOLD_INGOT)));
        unlocks.add(new UnlockInfo(85, "Netherite Smithing", new ItemStack(Items.NETHERITE_SCRAP)));
    }

    private static void populateCookingUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Beef, Pork & Mutton", new ItemStack(Items.COOKED_BEEF)));
        unlocks.add(new UnlockInfo(25, "Salmon & Chicken", new ItemStack(Items.COOKED_SALMON)));
        unlocks.add(new UnlockInfo(40, "Pie & Stews", new ItemStack(Items.PUMPKIN_PIE)));
        unlocks.add(new UnlockInfo(55, "Cake & Rabbit Stew", new ItemStack(Items.CAKE)));
    }

    private static void populateMobilityUnlocks(List<UnlockInfo> unlocks) {
        unlocks.add(new UnlockInfo(1, "Speed Scaling", new ItemStack(Items.LEATHER_BOOTS)));
        unlocks.add(new UnlockInfo(15, "Jump Scaling", new ItemStack(Items.RABBIT_FOOT)));
        unlocks.add(new UnlockInfo(30, "Fall Reduction", new ItemStack(Items.FEATHER)));
        unlocks.add(new UnlockInfo(45, "Swim Speed", new ItemStack(Items.PRISMARINE_SHARD)));
        unlocks.add(new UnlockInfo(60, "Wall Jumping", new ItemStack(Items.GOAT_HORN)));
        unlocks.add(new UnlockInfo(80, "Water Striding", new ItemStack(Items.LILY_PAD)));
    }
}