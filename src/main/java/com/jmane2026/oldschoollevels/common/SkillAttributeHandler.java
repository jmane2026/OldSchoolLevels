package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class SkillAttributeHandler {
    private static final Identifier LIFE_ID = Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "skill_life_bonus");
    private static final Identifier STRENGTH_ID = Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "skill_strength_bonus");

    public static void refreshAttributes(ServerPlayer player) {
        SkillData data = player.getData(ModAttachments.SKILLS);
        
        // Life Scaling: +0.5 HP per level above 10
        int lifeLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.LIFE));
        double hpBonus = Math.max(0, (lifeLvl - 10) * 0.5);

        // Strength Scaling: +0.1 Attack Damage per level, modified by Combat Style
        CombatStyle style = player.getData(ModAttachments.COMBAT_STYLE);
        int strLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.STRENGTH));
        double baseStrBonus = (strLvl - 1) * 0.1;
        double strBonus = baseStrBonus * style.getStrengthScale();

        applyModifier(player, Attributes.MAX_HEALTH, LIFE_ID, hpBonus);
        applyModifier(player, Attributes.ATTACK_DAMAGE, STRENGTH_ID, strBonus);
    }

    private static void applyModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            if (amount > 0) {
                instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshAttributes(player);
        }
    }
}