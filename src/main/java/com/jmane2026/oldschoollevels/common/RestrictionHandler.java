package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.items.EchoItem;
import com.jmane2026.oldschoollevels.client.gui.WarningOverlay;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.world.level.levelgen.structure.StructureStart;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class RestrictionHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SkillData data = player.getData(ModAttachments.SKILLS.get());
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();

        ItemStack held = player.getMainHandItem();
        List<String> missingReqs = new ArrayList<>();

        // 1. Tool Requirement Check (Dual Attack + Skill)
        if (!held.isEmpty()) {
            String path = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
            int reqAtk = RequirementUtils.getRequiredAttackLevel(held);
            int atkLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ATTACK));
            int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
            int wcLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));

            if (atkLvl < reqAtk) {
                missingReqs.add("Lvl " + reqAtk + " Attack");
            }

            if (path.contains("pickaxe") && miningLvl < reqAtk) {
                missingReqs.add("Lvl " + reqAtk + " Mining");
            } else if (path.contains("_axe") && wcLvl < reqAtk) {
                missingReqs.add("Lvl " + reqAtk + " Woodcutting");
            }
        }

        // 2. Block Requirement Check
        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int reqMining = RequirementUtils.getRequiredMiningLevel(block);
        if (miningLvl < reqMining && reqMining > 1) {
            missingReqs.add("Lvl " + reqMining + " Mining");
        }

        int wcLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));
        int reqWc = RequirementUtils.getRequiredWoodcuttingLevel(block);
        if (wcLvl < reqWc && reqWc > 1 && !missingReqs.contains("Lvl " + reqWc + " Woodcutting")) {
            missingReqs.add("Lvl " + reqWc + " Woodcutting");
        }

        if (!missingReqs.isEmpty()) {
            WarningOverlay.showWarning("Requires: " + String.join(", ", missingReqs));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        SkillData data = player.getData(ModAttachments.SKILLS.get());
        ItemStack held = player.getMainHandItem();

        // Tool Check first
        int atkLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ATTACK));
        int reqAtk = RequirementUtils.getRequiredAttackLevel(held);
        if (atkLvl < reqAtk) {
            event.setCanceled(true);
            return;
        }

        // Mining Check
        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int reqMining = RequirementUtils.getRequiredMiningLevel(event.getState().getBlock());

        // Dual Requirement: If holding a pickaxe, check Mining level requirement of the tool too
        String heldPath = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();

        if (heldPath.contains("pickaxe")) {
            int toolMiningReq = RequirementUtils.getRequiredMiningLevel(Blocks.IRON_ORE); // Placeholder logic for tool tier
            // Realistically, we can just check if the player can use the tool material:
            int toolTierReq = RequirementUtils.getRequiredAttackLevel(held);
            if (miningLvl < toolTierReq) {
                player.sendSystemMessage(Component.literal("§cYou need Level " + toolTierReq + " Mining to use this Pickaxe!"), true);
                event.setCanceled(true);
                return;
            }
        }

        if (miningLvl < reqMining) {
            WarningOverlay.showWarning("Requires Level " + reqMining + " Mining");
            event.setCanceled(true);
            return;
        }

        // Woodcutting Check
        int wcLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));
        int reqWc = RequirementUtils.getRequiredWoodcuttingLevel(event.getState().getBlock());
        if (heldPath.contains("_axe")) {
            int toolTierReq = RequirementUtils.getRequiredAttackLevel(held);
            if (wcLvl < toolTierReq) {
                WarningOverlay.showWarning("Requires Level " + toolTierReq + " Woodcutting");
                event.setCanceled(true);
            }
        }

        // Ranged Requirement Check
        int rangedLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));
        int reqRanged = RequirementUtils.getRequiredRangedLevel(held);
        if (rangedLvl < reqRanged && heldPath.contains("_bow")) {
            WarningOverlay.showWarning("Requires Level " + reqRanged + " Ranged");
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SkillData data = player.getData(ModAttachments.SKILLS.get());

        // Navigation Logic: Clear target if no Echo is held in either hand
        // This is now above the isEmpty check so it works when swapping to an empty hand
        boolean holdingEcho = player.getMainHandItem().getItem() instanceof EchoItem ||
                player.getOffhandItem().getItem() instanceof EchoItem;

        if (!holdingEcho && !player.getData(ModAttachments.ECHO_TARGET.get()).equals(BlockPos.ZERO)) {
            player.setData(ModAttachments.ECHO_TARGET.get(), BlockPos.ZERO);
            player.syncData(ModAttachments.ECHO_TARGET.get());
        }

        ItemStack stack = event.getTo();
        if (stack.isEmpty()) return;

        int defLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.DEFENSE));
        int atkLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ATTACK));
        int rangedLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));
        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int wcLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));

        int reqDef = RequirementUtils.getRequiredDefenseLevel(stack);
        int reqAtk = RequirementUtils.getRequiredAttackLevel(stack);
        int reqRanged = RequirementUtils.getRequiredRangedLevel(stack);
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        List<String> missing = new ArrayList<>();
        
        if (atkLvl < reqAtk) missing.add("Lvl " + reqAtk + " Attack");
        if (defLvl < reqDef) missing.add("Lvl " + reqDef + " Defense");
        if (rangedLvl < reqRanged && path.contains("_bow")) missing.add("Lvl " + reqRanged + " Ranged");

        // Dual Requirement for Tools (Checking corresponding skill against the tool's tier)
        if (path.contains("pickaxe") && miningLvl < reqAtk) {
            missing.add("Lvl " + reqAtk + " Mining");
        } else if (path.contains("_axe") && wcLvl < reqAtk) {
            missing.add("Lvl " + reqAtk + " Woodcutting");
        }

        if (!missing.isEmpty()) {
            WarningOverlay.showWarning("Requires: " + String.join(", ", missing));
            
            // Force unequip: Try to put it in inventory, otherwise drop it
            EquipmentSlot slot = event.getSlot();
            if (!player.getInventory().add(stack.copy())) {
                player.drop(stack.copy(), false);
            }
            // Clear the slot so they aren't wearing/holding it
            player.getItemBySlot(slot).setCount(0);
        }

        // Instant Navigation Sync on Wield
        if (stack.getItem() instanceof EchoItem echo) {
            echo.updateAndSync(player, player.level());
        }
    }

    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        SkillData data = player.getData(ModAttachments.SKILLS.get());
        int rangedLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));

        // We scan for the "Best" arrow that the player actually has the level for.
        // Priority: Highest Tier Usable
        ItemStack bestUsable = ItemStack.EMPTY;
        float bestDmg = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // Check if item is in the arrows tag
            if (stack.is(ItemTags.ARROWS)) {
                int req = RequirementUtils.getRequiredRangedLevel(stack);

                // If the player meets the requirement, check if it's better than our current choice
                if (rangedLvl >= req) {
                    String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                    float dmg = RequirementUtils.getArrowDamage(path);

                    if (dmg > bestDmg) {
                        bestDmg = dmg;
                        bestUsable = stack;
                    }
                }
            }
        }

        // If we found a valid arrow that is different from what vanilla picked,
        // we override the projectile selection.
        if (!bestUsable.isEmpty()) {
            event.setProjectileItemStack(bestUsable);
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Find the ammo the player is actually about to fire
        ItemStack ammo = player.getProjectile(event.getBow());
        if (ammo.isEmpty()) return;

        SkillData data = player.getData(ModAttachments.SKILLS.get());
        int rangedLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));
        int reqRanged = RequirementUtils.getRequiredRangedLevel(ammo);

        if (rangedLvl < reqRanged) {
            WarningOverlay.showWarning("Requires Level " + reqRanged + " Ranged to use these arrows");
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFishLoot(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SkillData data = player.getData(ModAttachments.SKILLS.get());
        int fishLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.FISHING));

        List<ItemStack> originalDrops = event.getDrops();
        List<ItemStack> newDrops = new ArrayList<>();

        for (ItemStack stack : originalDrops) {
            int req = RequirementUtils.getRequiredFishingLevel(stack);
            
            // 1. Restriction Check
            if (fishLvl < req) {
                WarningOverlay.showWarning("Requires Level " + req + " Fishing");
                continue; // Remove the item
            }

            // 2. Junk Reduction Logic
            // Identify junk (Vanilla tags it as 'fishing_loot/junk' internally, but we can check item list)
            boolean isJunk = stack.is(Items.BOWL) || stack.is(Items.LEATHER_BOOTS) || stack.is(Items.ROTTEN_FLESH) || 
                             stack.is(Items.STICK) || stack.is(Items.STRING) || stack.is(Items.GLASS_BOTTLE) || 
                             stack.is(Items.BONE) || stack.is(Items.INK_SAC);

            if (isJunk) {
                // Linear reduction: At level 1, 1% reduction. At level 99, ~100% reduction.
                if (player.getRandom().nextFloat() < (fishLvl / 100.0f)) {
                    continue; // Trash successfully avoided
                }
            }
            newDrops.add(stack);
        }
        // Update the event drops
        originalDrops.clear();
        originalDrops.addAll(newDrops);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) return;

        SkillData data = player.getData(ModAttachments.SKILLS.get());
        int smithLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.SMITHING));
        int cookLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.COOKING));
        int fletchLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.FLETCHING));
        int arcanaLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ARCANA));

        // Handle Furnace logic to prevent fuel waste and stop restricted smelting
        if (menu instanceof AbstractFurnaceMenu furnace) {
            Slot inputSlot = menu.getSlot(0);
            Slot fuelSlot = menu.getSlot(1);
            ItemStack inputStack = inputSlot.getItem();
            // Capture the fuel item type at the start of the tick
            Item capturedFuelType = fuelSlot.getItem().getItem();

            if (!inputStack.isEmpty()) {
                int reqSmith = RequirementUtils.getRequiredSmithingLevel(inputStack);
                int reqCook = RequirementUtils.getRequiredCookingLevel(inputStack);

                if (smithLvl < reqSmith || cookLvl < reqCook) {
                    // Determine if the furnace ignited this tick (Lit but no progress yet)
                    boolean wasJustLit = furnace.isLit();

                    // 1. Reset logic via the menu instance (using setProperty to avoid AttachmentType conflict)
                    furnace.setData(0, 0); // litTimeRemaining
                    furnace.setData(1, 0); // litTotalTime
                    furnace.setData(2, 0); // cookingTimer

                    // 2. Kill the visual fire on the physical block in the world
                    if (inputSlot.container instanceof AbstractFurnaceBlockEntity furnaceEntity) {
                        BlockState worldState = furnaceEntity.getBlockState();
                        if (worldState.hasProperty(AbstractFurnaceBlock.LIT) && worldState.getValue(AbstractFurnaceBlock.LIT)) {
                            player.level().setBlock(furnaceEntity.getBlockPos(), worldState.setValue(AbstractFurnaceBlock.LIT, false), 3);
                        }
                    }

                    // 3. Eject the restricted input item
                    ejectItem(player, inputSlot);

                    // 4. Refund the 1 fuel directly back into the furnace slot
                    if (wasJustLit && capturedFuelType != Items.AIR) {
                        int currentCount = fuelSlot.getItem().getCount();
                        fuelSlot.set(new ItemStack(capturedFuelType, currentCount + 1));
                    }

                    menu.broadcastChanges();
                    WarningOverlay.showWarning("Requires Level " + Math.max(reqSmith, reqCook));
                    return;
                }
            }
        }

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            int reqSmith = RequirementUtils.getRequiredSmithingLevel(stack);
            int reqCook = RequirementUtils.getRequiredCookingLevel(stack);
            int reqFletch = RequirementUtils.getRequiredFletchingLevel(stack);
            int reqArcana = RequirementUtils.getRequiredArcanaLevel(stack);

            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            boolean isFletchingItem = path.contains("_knife") || path.contains("_bow") || path.contains("_arrow") || path.contains("_heads") || stack.is(Items.STICK);
            boolean isResult = slot instanceof ResultSlot || slot.container instanceof ResultContainer;
            if (isResult) {
                List<String> missing = new ArrayList<>();
                if (smithLvl < reqSmith) missing.add("Lvl " + reqSmith + " Smithing");
                if (cookLvl < reqCook) missing.add("Lvl " + reqCook + " Cooking");
                if (isFletchingItem && fletchLvl < reqFletch) missing.add("Lvl " + reqFletch + " Fletching");
                if (arcanaLvl < reqArcana) missing.add("Lvl " + reqArcana + " Arcana");

                boolean restricted = !missing.isEmpty();

                // Element Proximity Check for Sigil Conversion
                if (path.contains("_sigil") && !path.contains("blank") && !path.contains("raw")) {
                    if (!isNearElementalDome(player, path)) {
                        WarningOverlay.showWarning("You are not in the correct location to imbue this Sigil");
                        restricted = true;
                        } else if (restricted) {
                        // If they ARE in the dome but lack the level, show the level requirement
                        WarningOverlay.showWarning("Requires: " + String.join(", ", missing));
                        }
                    } else if (restricted) {
                    WarningOverlay.showWarning("Requires: " + String.join(", ", missing));
                }

                if (restricted) {
                    slot.set(ItemStack.EMPTY);
                    menu.broadcastChanges();
                }
            }
        }
    }

    private static boolean isNearElementalDome(ServerPlayer player, String sigilPath) {
        // Corrected parsing: Handle namespace properly to get just "air", "water", etc.
        String element = sigilPath;
        if (element.contains(":")) {
            element = element.split(":")[1]; // Get "air_sigil"
        }
        element = element.split("_")[0]; // Get "air"

        // Target the structure tag we defined: #oldschoollevels:air_domes
        TagKey<Structure> structureTag = TagKey.create(Registries.STRUCTURE,
                Identifier.fromNamespaceAndPath("oldschoollevels", element + "_domes"));

        BlockPos playerPos = player.blockPosition();
        // Debugging: Log player's position

        // Check at player's feet
        StructureStart startAtFeet = player.level().structureManager()
                .getStructureWithPieceAt(playerPos, structureTag);

        if (startAtFeet.isValid()) {
            return true;
        }

        // Check at player's eye level (or slightly above feet)
        BlockPos playerEyePos = playerPos.above(); // Or player.getEyeBlockPosition()
        StructureStart startAtEye = player.level().structureManager()
                .getStructureWithPieceAt(playerEyePos, structureTag);

        if (startAtEye.isValid()) {
            return true;
        }
        return false;
    }

    private static void ejectItem(ServerPlayer player, Slot slot) {
        ItemStack stack = slot.getItem().copy();
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
            return;
        }

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        slot.set(ItemStack.EMPTY);
    }
}