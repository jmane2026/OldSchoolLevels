package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.DamageNumberPayload;
import com.jmane2026.oldschoollevels.network.XpGainPayload;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.player.Player;
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

    private static final float BASE_CRIT_CHANCE = 0.05f; // 5% chance

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        // Handle Critical Hits for Players
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            boolean isCrit = player.getRandom().nextFloat() < BASE_CRIT_CHANCE;
            if (isCrit) {
                event.setAmount(event.getAmount() * 2);
            }
            player.setData(ModAttachments.IS_CRITICAL, isCrit);
        }
    }

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
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        Block block = state.getBlock();
        SkillData data = player.getData(ModAttachments.SKILLS);

        float originalSpeed = event.getOriginalSpeed();
        float newSpeed = originalSpeed;

        // Mining Speed Scaling
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            int level = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
            int reqLevel = RequirementUtils.getRequiredMiningLevel(block);
            
            if (level < reqLevel) {
                newSpeed = 0f; // Prevent progress and durability loss
            } else {
                float tierMultiplier = getMiningSpeedMultiplier(block);
                newSpeed += originalSpeed * ((level - reqLevel) * tierMultiplier);
            }
        }

        // Woodcutting Speed Scaling
        if (state.is(BlockTags.LOGS)) {
            int level = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));
            int reqLevel = RequirementUtils.getRequiredWoodcuttingLevel(block);
            
            if (level < reqLevel) {
                newSpeed = 0f; // Prevent progress and durability loss
            } else {
                float tierMultiplier = getWoodcuttingSpeedMultiplier(block);
                newSpeed += originalSpeed * ((level - reqLevel) * tierMultiplier);
            }
        }

        if (newSpeed != originalSpeed) {
            event.setNewSpeed(newSpeed);
        }
    }

    @SubscribeEvent
    public static void onCombat(LivingDamageEvent.Post event) {
        // 1. Handle OUTGOING Damage (Player hits something)
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            float baseDamage = event.getNewDamage();
            float finalDamage = baseDamage;

            // Ranged Scaling Logic
            if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
                SkillData data = player.getData(ModAttachments.SKILLS.get());
                int rangedLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));

                // 1. Get Arrow Tier Damage using getPickupItemStackOrigin() from AbstractArrow.java source
                ItemStack arrowStack = arrow.getPickupItemStackOrigin();
                String arrowPath = !arrowStack.isEmpty() ? BuiltInRegistries.ITEM.getKey(arrowStack.getItem()).getPath() : "";
                
                float arrowBonus = Math.max(0, RequirementUtils.getArrowDamage(arrowPath) - 1.0f);
                finalDamage += arrowBonus;

                // 2. Get Bow Tier Damage (factors in material of the held bow)
                ItemStack bow = player.getMainHandItem();
                String bowPath = BuiltInRegistries.ITEM.getKey(bow.getItem()).getPath();
                finalDamage += RequirementUtils.getBowDamageBonus(bowPath);

                // 3. Final scaling: 1% bonus damage per Ranged level
                finalDamage *= (1.0f + (rangedLvl / 100.0f));
            }

            float damage = finalDamage;

            if (damage <= 0) return;

            boolean wasCrit = player.getData(ModAttachments.IS_CRITICAL);

            // Send Damage Splat packet (isIncoming = false)
            PacketDistributor.sendToPlayer(player, new DamageNumberPayload(
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), 
                    damage, wasCrit, false
            ));
            
            player.setData(ModAttachments.IS_CRITICAL, false); // Reset flag

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

        // 2. Handle INCOMING Damage (Something hits Player)
        if (event.getEntity() instanceof ServerPlayer victim) {
            float incomingDmg = event.getNewDamage();
            if (incomingDmg > 0) {
                PacketDistributor.sendToPlayer(victim, new DamageNumberPayload(
                        victim.getX(), victim.getY(), victim.getZ(), incomingDmg, false, true
                ));
            }
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
            String path = BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();
            long count = item.getCount();

            // 1. Handle Stick Fletching (XP based on quantity)
            if (item.is(Items.STICK)) {
                awardXp(player, Skill.FLETCHING, 1L * count);
            }
            
            // 2. Handle Knives
            else if (path.contains("_knife")) {
                awardXp(player, Skill.FLETCHING, 15L);
            }
            
            // 3. Handle Arrow Heads
            else if (path.contains("_arrow_heads")) {
                awardXp(player, Skill.FLETCHING, 2L * count);
            }
            
            // 4. Handle Arrows
            else if (path.contains("arrow") || item.is(Items.ARROW)) {
                awardXp(player, Skill.FLETCHING, 5L * count);
            }
            
            // 5. Handle Bows
            else if (path.contains("_bow") || item.is(Items.BOW)) {
                awardXp(player, Skill.FLETCHING, getBowFletchXp(path));
            }
            
            if (isMetalGear(item)) {
                awardXp(player, Skill.SMITHING, 50L);
            }
        }
    }

    private static long getBowFletchXp(String path) {
        if (path.contains("cherry")) return 150;
        if (path.contains("mangrove")) return 125;
        if (path.contains("dark_oak")) return 100;
        if (path.contains("acacia")) return 80;
        if (path.contains("jungle")) return 60;
        if (path.contains("spruce") || path.contains("birch")) return 40;
        return 25; // Oak
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

        // Refresh attributes if any combat-related scaling skill was increased
        if (skill == Skill.LIFE || skill == Skill.STRENGTH ||
                skill == Skill.ATTACK || skill == Skill.DEFENSE) {
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

        return 25;
    }

    private static float getMiningSpeedMultiplier(Block block) {
        // Tier 0: Basic stones (2% per level)
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) return 0.02f;
        
        // Tier 1: Common Ores (Coal, Iron) (1.5% per level)
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
            block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE || 
            block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 0.015f;

        // Tier 2: Rare Ores (1% per level)
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || 
            block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE || 
            block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 0.01f;

        // Tier 3: Elite Ores (0.5% per level)
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE || 
            block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
            block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE) return 0.005f;

        // Tier 4: Legendary (0.2% per level)
        if (block == Blocks.ANCIENT_DEBRIS) return 0.002f;

        // Default for unrecognized ores or copper
        return 0.015f; 
    }

    private static float getWoodcuttingSpeedMultiplier(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        
        if (block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG || block == Blocks.BIRCH_LOG) return 0.02f; // 2%
        if (block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG) return 0.015f; // 1.5%
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG) return 0.01f; // 1%
        
        return 0.01f;
    }

    private static long getFishXp(ItemStack stack) {
        if (stack.is(Items.COD)) return 10;
        if (stack.is(Items.SALMON)) return 70;
        if (stack.is(Items.PUFFERFISH)) return 100;
        if (stack.is(Items.TROPICAL_FISH)) return 150;
        return 5; // Junk or other item
    }

    private static long getFoodXp(ItemStack stack) {
        if (stack.is(Items.BREAD)) return 25;
        if (stack.is(Items.PUMPKIN_PIE)) return 40;
        if (stack.is(Items.CAKE)) return 100;
        if (stack.is(Items.MUSHROOM_STEW) || stack.is(Items.SUSPICIOUS_STEW)) return 50;
        if (stack.is(Items.RABBIT_STEW)) return 80;
        if (stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP)) return 30;
        if (stack.is(Items.COOKED_RABBIT)) return 30;
        if (stack.is(Items.COOKED_MUTTON)) return 40;
        if (stack.is(Items.COOKED_SALMON)) return 50;
        if (stack.is(Items.COOKED_COD)) return 100;
        return 15;
    }

    private static boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_CHICKEN) ||
                stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.COOKED_MUTTON) ||
                stack.is(Items.COOKED_COD) || stack.is(Items.COOKED_SALMON) ||
                stack.is(Items.COOKED_RABBIT);
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