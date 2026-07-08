package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class ClientTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        IAttachmentHolder holder = (IAttachmentHolder) mc.player;
        SkillData data = holder.getData(ModAttachments.SKILLS.get());

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        // 1. Combat Requirements (Weaponry & Armor)
        addRequirement(event.getToolTip(), "Attack", RequirementUtils.getRequiredAttackLevel(stack), Skill.ATTACK, data);
        addRequirement(event.getToolTip(), "Defense", RequirementUtils.getRequiredDefenseLevel(stack), Skill.DEFENSE, data);

        // 2. Functional Tool Requirements (Mining & Woodcutting)
        if (path.contains("pickaxe")) {
            addRequirement(event.getToolTip(), "Mining", RequirementUtils.getRequiredAttackLevel(stack), Skill.MINING, data);
        } else if (path.contains("_axe")) {
            addRequirement(event.getToolTip(), "Woodcutting", RequirementUtils.getRequiredAttackLevel(stack), Skill.WOODCUTTING, data);
        }

        // 3. Processing Requirements (Only for Ores and Food)
        boolean isMetalMaterial = path.contains("raw_") || path.contains("_ore") || path.contains("_ingot") || path.contains("scrap");
        if (isMetalMaterial) {
            addRequirement(event.getToolTip(), "Smithing", RequirementUtils.getRequiredSmithingLevel(stack), Skill.SMITHING, data);
        }

        int cookReq = RequirementUtils.getRequiredCookingLevel(stack);
        if (cookReq > 1) {
            addRequirement(event.getToolTip(), "Cooking", cookReq, Skill.COOKING, data);
        }
    }

    private static void addRequirement(List<Component> tooltip, String label, int req, Skill skill, SkillData data) {
        if (req <= 1) return;

        int currentLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(skill));
        String color = currentLevel >= req ? "§a" : "§c";

        tooltip.add(Component.literal("§7Requires " + color + "Level " + req + " " + label));
    }
}