package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.XpGainPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class LevelingHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockState state = event.getState();
        Block block = state.getBlock();

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            awardXp(player, Skill.MINING, getMiningXp(block));
        }

        if (state.is(BlockTags.LOGS)) {
            awardXp(player, Skill.WOODCUTTING, getWoodcuttingXp(block));
        }
    }

    @SubscribeEvent
    public static void onCombat(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            float damage = event.getNewDamage();
            if (damage <= 0) return;

            // OSRS Scale: ~4 XP per 1 damage point (scaled to MC hearts)
            long combatXp = Math.round(damage * 4);
            long lifeXp = Math.round(damage * 1.33);

            if (event.getSource().getDirectEntity() instanceof AbstractArrow) {
                awardXp(player, Skill.RANGED, combatXp);
            } else {
                CombatStyle style = player.getData(ModAttachments.COMBAT_STYLE);
                switch (style) {
                    case ACCURATE -> awardXp(player, Skill.ATTACK, combatXp);
                    case AGGRESSIVE -> awardXp(player, Skill.STRENGTH, combatXp);
                    case DEFENSIVE -> awardXp(player, Skill.DEFENSE, combatXp);
                    case CONTROLLED -> {
                        long splitXp = combatXp / 3;
                        awardXp(player, Skill.ATTACK, splitXp);
                        awardXp(player, Skill.STRENGTH, splitXp);
                        awardXp(player, Skill.DEFENSE, splitXp);
                    }
                }
            }
            awardXp(player, Skill.LIFE, lifeXp);
        }
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack item = event.getSmelting();
            if (isFood(item)) {
                awardXp(player, Skill.COOKING, getFoodXp(item) * item.getCount());
            } else if (item.is(Items.IRON_INGOT) || item.is(Items.GOLD_INGOT) || item.is(Items.COPPER_INGOT)) {
                awardXp(player, Skill.SMITHING, 25L * item.getCount());
            } else if (item.is(Items.NETHERITE_SCRAP)) {
                awardXp(player, Skill.SMITHING, 100L * item.getCount());
            }
        }
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack item = event.getCrafting();
            
            if (isFletchingItem(item)) {
                awardXp(player, Skill.FLETCHING, getFletchingXp(item));
            }
            
            if (isMetalGear(item)) {
                awardXp(player, Skill.SMITHING, 50L);
            }
        }
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Iterate through drops to award XP based on what was actually caught
            for (ItemStack stack : event.getDrops()) {
                awardXp(player, Skill.FISHING, getFishXp(stack));
            }
        }
    }

    private static void awardXp(ServerPlayer player, Skill skill, long amount) {
        if (amount <= 0) return;
        SkillData currentData = player.getData(ModAttachments.SKILLS);
        SkillData newData = currentData.addExperience(skill, amount);

        player.setData(ModAttachments.SKILLS, newData);

        // Refresh attributes if a scaling skill was increased
        if (skill == Skill.LIFE || skill == Skill.STRENGTH) {
            SkillAttributeHandler.refreshAttributes(player);
        }

        PacketDistributor.sendToPlayer(player, new XpGainPayload(skill, amount, newData.getExperience(skill)));
    }

    private static long getMiningXp(Block block) {
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 17;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 35;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 17;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return 65;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 40;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 30;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 121;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 160;
        if (block == Blocks.NETHER_QUARTZ_ORE) return 50;
        if (block == Blocks.NETHER_GOLD_ORE) return 50;
        if (block == Blocks.ANCIENT_DEBRIS) return 150;
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) return 1;
        return 0;
    }

    private static long getWoodcuttingXp(Block block) {
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_WOOD) return 37;
        if (block == Blocks.SPRUCE_LOG || block == Blocks.SPRUCE_WOOD) return 45;
        if (block == Blocks.BIRCH_LOG || block == Blocks.BIRCH_WOOD) return 45;
        if (block == Blocks.JUNGLE_LOG || block == Blocks.JUNGLE_WOOD) return 40;
        if (block == Blocks.ACACIA_LOG || block == Blocks.ACACIA_WOOD) return 50;
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.DARK_OAK_WOOD) return 67;
        if (block == Blocks.MANGROVE_LOG || block == Blocks.MANGROVE_WOOD) return 60;
        if (block == Blocks.CHERRY_LOG || block == Blocks.CHERRY_WOOD) return 60;
        
        // Custom check for Poplar logs by registry name
        String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (blockName.contains("poplar")) return 55;

        return 25;
    }

    private static long getFishXp(ItemStack stack) {
        if (stack.is(Items.COD)) return 10;
        if (stack.is(Items.SALMON)) return 70;
        if (stack.is(Items.PUFFERFISH)) return 100;
        if (stack.is(Items.TROPICAL_FISH)) return 150;
        return 5; // Junk or other items
    }

    private static long getFoodXp(ItemStack stack) {
        if (stack.is(Items.BREAD)) return 25;
        if (stack.is(Items.PUMPKIN_PIE)) return 40;
        if (stack.is(Items.CAKE)) return 100;
        if (stack.is(Items.MUSHROOM_STEW) || stack.is(Items.SUSPICIOUS_STEW)) return 50;
        if (stack.is(Items.RABBIT_STEW)) return 80;
        if (stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP)) return 30;
        return 15;
    }

    private static boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_CHICKEN) ||
                stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.COOKED_MUTTON) ||
                stack.is(Items.COOKED_COD) || stack.is(Items.COOKED_SALMON);
    }

    private static boolean isFletchingItem(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return stack.is(Items.ARROW) || stack.is(Items.BOW) || path.contains("bow") || path.contains("arrow");
    }

    private static long getFletchingXp(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        long count = stack.getCount();

        if (stack.is(Items.ARROW)) {
            // Standard arrows (flint)
            return 5 * count;
        }
        
        // Handle various arrow types by name
        if (path.contains("iron_arrow")) return 10 * count;
        if (path.contains("diamond_arrow")) return 15 * count;

        if (stack.is(Items.BOW)) return 25;
        
        // Handle custom wood bows
        if (path.contains("poplar_bow")) return 40;
        if (path.contains("oak_bow")) return 25;
        if (path.contains("dark_oak_bow")) return 60;

        return 10;
    }

    private static boolean isMetalGear(ItemStack stack) {
        Item item = stack.getItem();
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        
        // Check for Armor specifically to ensure Smithing XP is targeted correctly
        boolean isArmor = path.contains("helmet") || path.contains("chestplate") || 
                         path.contains("leggings") || path.contains("boots");
        
        boolean isTools = path.contains("sword") || path.contains("pickaxe") || 
                         path.contains("axe") || path.contains("shovel") || path.contains("hoe");

        boolean isMetal = path.contains("iron") || path.contains("gold") || 
                         path.contains("diamond") || path.contains("netherite") || path.contains("copper");
                         
        return isMetal && (isArmor || isTools);
    }
}