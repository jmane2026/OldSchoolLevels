package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class ClientTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        IAttachmentHolder holder = mc.player;
        SkillData data = holder.getData(ModAttachments.SKILLS.get());

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // 0. Damage Statistics Display
        // Following vanilla weapon methodology (Green text for attribute-style values)
        if (path.contains("_knife")) {
            float dmg = RequirementUtils.getKnifeDamage(path);
            float speed = RequirementUtils.getKnifeSpeed(path);

            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.literal(" " + dmg + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN));
            event.getToolTip().add(Component.literal(" " + speed + " Attack Speed").withStyle(ChatFormatting.DARK_GREEN));
        }

        if (path.contains("_arrow") && !path.contains("_heads")) {
            float dmg = RequirementUtils.getArrowDamage(path);
            event.getToolTip().add(Component.literal("+" + (dmg) + " to Attack Damage").withStyle(ChatFormatting.DARK_GREEN));
        }

        if (path.contains("_bow")) {
            float bowBonus = RequirementUtils.getBowDamageBonus(path);
            ItemStack bestArrow = findAmmo(mc.player);

            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));

            if (!bestArrow.isEmpty()) {
                String arrowPath = BuiltInRegistries.ITEM.getKey(bestArrow.getItem()).getPath();
                float arrowMod = Math.max(0, RequirementUtils.getArrowDamage(arrowPath) - 1.0f);
                // 9.0 (Vanilla Max Charge) + Bow Tier + Arrow Tier Modifier
                float totalDmg = 9.0f + bowBonus + arrowMod;
                event.getToolTip().add(Component.literal(" " + totalDmg + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN));
            } else {
                event.getToolTip().add(Component.literal(" " + (9.0f + bowBonus) + " Attack Damage").withStyle(ChatFormatting.DARK_GREEN));
            }
            event.getToolTip().add(Component.literal(" 1.0 Attack Speed").withStyle(ChatFormatting.DARK_GREEN));
        }

        // Collect all requirements into a list first to determine if we need a header
        java.util.List<Component> reqLines = new java.util.ArrayList<>();

        // 1. Combat Requirements
        addRequirement(reqLines, "Attack", RequirementUtils.getRequiredAttackLevel(stack), Skill.ATTACK, data);
        addRequirement(reqLines, "Defense", RequirementUtils.getRequiredDefenseLevel(stack), Skill.DEFENSE, data);
        addRequirement(reqLines, "Ranged", RequirementUtils.getRequiredRangedLevel(stack), Skill.RANGED, data);

        // 2. Functional Tool Requirements
        int tierReq = RequirementUtils.getRequiredAttackLevel(stack);
        if (path.contains("pickaxe")) {
            addRequirement(reqLines, "Mining", tierReq, Skill.MINING, data);
        } else if (path.contains("_axe")) {
            addRequirement(reqLines, "Woodcutting", tierReq, Skill.WOODCUTTING, data);
        }

        // 3. Fletching (Heads, Sticks)
        boolean isFletchingItem = path.contains("_heads") || stack.is(net.minecraft.world.item.Items.STICK);
        if (isFletchingItem) {
            addRequirement(reqLines, "Fletching", RequirementUtils.getRequiredFletchingLevel(stack), Skill.FLETCHING, data);
        }

        // 4. Processing Requirements
        int smithReq = RequirementUtils.getRequiredSmithingLevel(stack);
        if (smithReq > 1) {
            boolean isGear = path.contains("sword") || path.contains("pickaxe") || path.contains("axe") || path.contains("shovel") || path.contains("hoe") || path.contains("helmet") || path.contains("chestplate") || path.contains("leggings") || path.contains("boots") || path.contains("spear");
            if (!isGear) {
                addRequirement(reqLines, "Smithing", smithReq, Skill.SMITHING, data);
            }
        }

        // 5. Block Breaking (Mining/Woodcutting) Requirements
        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
            net.minecraft.world.level.block.Block block = blockItem.getBlock();
            int miningReq = RequirementUtils.getRequiredMiningLevel(block);
            if (miningReq > 1) {
                addRequirement(reqLines, "Mining", miningReq, Skill.MINING, data);
            }
            int wcReq = RequirementUtils.getRequiredWoodcuttingLevel(block);
            if (wcReq > 1) {
                addRequirement(reqLines, "Woodcutting", wcReq, Skill.WOODCUTTING, data);
            }
        }

        int cookReq = RequirementUtils.getRequiredCookingLevel(stack);
        if (cookReq > 1) {
            addRequirement(reqLines, "Cooking", cookReq, Skill.COOKING, data);
        }

        // If any requirements were added, render them with the "Requires:" header
        if (!reqLines.isEmpty()) {
            event.getToolTip().add(Component.empty()); // Spacer before section
            event.getToolTip().add(Component.literal("Requires:").withStyle(ChatFormatting.GRAY));
            event.getToolTip().addAll(reqLines);
        }
    }

    private static ItemStack findAmmo(Player player) {
        if (player == null) return ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.is(ItemTags.ARROWS)) {
                return invStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void addRequirement(List<Component> reqLines, String label, int req, Skill skill, SkillData data) {
        if (req <= 1) return;

        int currentLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(skill));
        String color = currentLevel >= req ? "§a" : "§c";

        // Indent levels with a space to match the attribute modifier look
        reqLines.add(Component.literal("  " + color + "Level " + req + " " + label));
    }
}