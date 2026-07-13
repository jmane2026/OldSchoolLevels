package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.ChangeStylePayload;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CharacterStatsScreen extends Screen {
    private static final int WIDTH = 190;
    private static final int HEIGHT = 250;

    public CharacterStatsScreen() {
        super(Component.literal("Character Sheet"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - (WIDTH / 2);
        int startY = centerY - (HEIGHT / 2);

        IAttachmentHolder holder = (IAttachmentHolder) Minecraft.getInstance().player;
        if (holder == null) return;

        int x = startX;
        int y = startY;

        // Add Style Selection Buttons
        CombatStyle[] styles = CombatStyle.values();
        for (int i = 0; i < styles.length; i++) {
            CombatStyle style = styles[i];
            this.addRenderableWidget(Button.builder(style.getName(), (btn) -> {
                holder.setData(ModAttachments.COMBAT_STYLE.get(), style);
                ClientPacketDistributor.sendToServer(new ChangeStylePayload(style));
            }).bounds(startX + 10, startY + 30 + (i * 22), 160, 20).build());
        }

        // Small "X" button in the top right corner
        this.addRenderableWidget(Button.builder(Component.literal("X"), (btn) -> this.onClose())
                .bounds(x + WIDTH - 18, y + 2, 14, 14)
                .build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_E || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Extraction of widgets happens first so they are rendered behind the background
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        IAttachmentHolder holder = (IAttachmentHolder) Minecraft.getInstance().player;
        if (holder == null) return;

        SkillData data = holder.getData(ModAttachments.SKILLS.get());
        CombatStyle currentStyle = holder.getData(ModAttachments.COMBAT_STYLE.get());

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - (WIDTH / 2);
        int startY = centerY - (HEIGHT / 2);

        // Draw Highlight for Selected Combat Style behind the background to match the buttons
        int activeIdx = currentStyle.ordinal();
        int highlightY = startY + 30 + (activeIdx * 22);
        graphics.outline(startX + 10 - 1, highlightY - 1, 162, 22, 0xFFFFFF00);

        // Draw Background
        graphics.fill(startX, startY, startX + WIDTH, startY + HEIGHT, 0xDD000000);
        graphics.outline(startX, startY, WIDTH, HEIGHT, 0xFFFFFFFF);

        // Title
        graphics.text(this.font, this.title, centerX - (this.font.width(this.title) / 2), startY + 8, 0xFFFFAA00);

        // Combat Section
        int combatY = startY + 122;
        graphics.text(this.font, "Combat Bonuses:", startX + 10, combatY, 0xFFBBBBBB);
        renderStat(graphics, "Strength:", getStrBonus(data, currentStyle), startX + 15, combatY + 12);
        renderStat(graphics, "Swing Speed:", getAtkBonus(data, currentStyle), startX + 15, combatY + 22);
        renderStat(graphics, "Armor:", getDefBonus(data, currentStyle), startX + 15, combatY + 32);
        renderStat(graphics, "Health:", getLifeBonus(data), startX + 15, combatY + 42);
        renderStat(graphics, "Ranged Dmg:", getRangedBonus(data), startX + 15, combatY + 52);
        renderStat(graphics, "Magic Bonus:", getMagicBonus(data), startX + 15, combatY + 62);
        
        int mobilityY = combatY + 72;
        renderStat(graphics, "Move Speed:", String.format("+%d%%", ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MOBILITY)) - 1), startX + 15, mobilityY);
        renderStat(graphics, "Max Stamina:", String.format("%.0f", RequirementUtils.getMaxStamina(ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MOBILITY)))), startX + 15, mobilityY + 10);

        // Gathering Section
        int gatherY = mobilityY + 22;
        graphics.text(this.font, "Gathering Bonuses:", startX + 10, gatherY, 0xFFBBBBBB);

        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int woodLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));

        renderStat(graphics, "Mining:", "+" + (miningLvl - 1) + "%", startX + 15, gatherY + 12);
        renderStat(graphics, "Wood Cutting:", "+" + (woodLvl - 1) + "%", startX + 15, gatherY + 22);
    }

    private void renderStat(GuiGraphicsExtractor graphics, String label, String value, int x, int y) {
        graphics.text(this.font, label, x, y, 0xFFAAAAAA);
        graphics.text(this.font, value, x + 100, y, 0xFFFFFF00);
    }

    private String getStrBonus(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.STRENGTH));
        double bonus = (lvl * style.getStrengthScale());
        return String.format("+%.1f%%", bonus);
    }

    private String getAtkBonus(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ATTACK));
        double bonus = (lvl - 1) * 0.05 * style.getAttackSpeedScale();
        return String.format("+%.2f", bonus);
    }

    private String getDefBonus(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.DEFENSE));
        double bonus = (lvl - 1) * 0.2 * style.getDefenseScale();
        return String.format("+%.1f", bonus);
    }

    private String getLifeBonus(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.LIFE));
        return String.format("+%.1f HP", Math.max(0, (lvl - 10) * 0.5));
    }

    private String getRangedBonus(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));
        return String.format("+%d%%", lvl);
    }

    private String getMagicBonus(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));
        // 1% per level logic: Level 6 = +6%
        return String.format("+%d%%", lvl);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}