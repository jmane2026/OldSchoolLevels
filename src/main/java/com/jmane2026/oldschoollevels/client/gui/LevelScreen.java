package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevelsClient;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;

import java.util.List;

public class LevelScreen extends Screen {
    private static final int PANEL_WIDTH = 145;
    private static final int PANEL_HEIGHT = 210;
    private static final int MARGIN = 10;
    private static final int BOX_SIZE = 40;
    private static final int SPACING = 4;

    public LevelScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (OldSchoolLevelsClient.LEVEL_SCREEN_KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Call super first to ensure the base screen state (background/blur) is handled before our custom UI
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Calculate bottom right position
        int x = this.width - PANEL_WIDTH - MARGIN;
        int y = this.height - PANEL_HEIGHT - MARGIN;

        // Draw main panel box (OSRS style dark grey background)
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xAA000000);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF); // Solid White Outline

        // Title
        graphics.text(this.font, this.title, x + (PANEL_WIDTH / 2) - (this.font.width(this.title) / 2), y + 5, 0xFFFFFFFF);

        int startX = x + 8;
        int startY = y + 18;
        int totalLevel = 0;
        Skill hoveredSkill = null;
        
        // Temporary variables for Combat Level calculation
        int atk = 1, def = 1, str = 1, hp = 10, range = 1;
        double pray = 1, mage = 1; // Not in list yet, but part of formula

        // Fetch the player's real skill data from Attachments
        SkillData skillData = Minecraft.getInstance().player != null 
                ? Minecraft.getInstance().player.getData(ModAttachments.SKILLS) 
                : SkillData.EMPTY;

        Skill[] skills = Skill.values();
        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            int col = i % 3;
            int row = i / 3;
            int bx = startX + (col * (BOX_SIZE + SPACING));
            int by = startY + (row * (BOX_SIZE + SPACING));

            // Draw Skill Box
            graphics.fill(bx, by, bx + BOX_SIZE, by + BOX_SIZE, 0x44FFFFFF);
            graphics.outline(bx, by, BOX_SIZE, BOX_SIZE, 0x88FFFFFF);

            long currentXp = skillData.getExperience(skill);
            int level = ExperienceUtils.getLevelAtExperience(currentXp);
            totalLevel += level;
            
            // Track combat stats for the summary
            if (skill == Skill.ATTACK) atk = level;
            if (skill == Skill.DEFENSE) def = level;
            if (skill == Skill.STRENGTH) str = level;
            if (skill == Skill.LIFE) hp = level;
            if (skill == Skill.RANGED) range = level;

            // Render Item Icon
            graphics.item(skill.getIcon(), bx + (BOX_SIZE / 2) - 8, by + 4);

            // Render Level Text
            String lvlStr = "Lvl: " + level + "/99";
            graphics.pose().pushMatrix();
            // Move to the horizontal center of the box and the desired Y position
            graphics.pose().translate(bx + (BOX_SIZE / 2f), by + 30f, graphics.pose());
            graphics.pose().scale(0.65f, 0.65f, graphics.pose());
            // Draw centered at 0 (which is the center of the box due to translation)
            graphics.text(this.font, lvlStr, (int) (-(this.font.width(lvlStr) / 2f)), 0, 0xFFFFFF00);
            graphics.pose().popMatrix();

            // Check Hover
            if (mouseX >= bx && mouseX <= bx + BOX_SIZE && mouseY >= by && mouseY <= by + BOX_SIZE) {
                hoveredSkill = skill;
            }
        }

        // Combat Level Calculation (OSRS Formula)
        double base = 0.25 * (def + hp + Math.floor(pray / 2));
        double melee = 0.325 * (atk + str);
        double ranged = 0.325 * Math.floor(3 * range / 2.0);
        double magic = 0.325 * Math.floor(3 * mage / 2.0);
        int combatLevel = (int) (base + Math.max(melee, Math.max(ranged, magic)));

        // Summary Info (Bottom area of the panel)
        String combatText = "Combat Lvl: " + combatLevel;
        String totalText = "Total Lvl: " + totalLevel;
        int ty = y + PANEL_HEIGHT - 16;

        // Render Combat Level (Left Aligned at bottom)
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8f, ty, graphics.pose());
        graphics.pose().scale(0.85f, 0.85f, graphics.pose());
        graphics.text(this.font, combatText, 0, 0, 0xFFFFAA00);
        graphics.pose().popMatrix();

        // Render Total Level (Right Aligned at bottom)
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + PANEL_WIDTH - 8f, ty, graphics.pose());
        graphics.pose().scale(0.85f, 0.85f, graphics.pose());
        graphics.text(this.font, totalText, (int) (-this.font.width(totalText)), 0, 0xFFFFAA00);
        graphics.pose().popMatrix();

        // Tooltip Rendering
        if (hoveredSkill != null) {
            long xp = skillData.getExperience(hoveredSkill);
            int lvl = ExperienceUtils.getLevelAtExperience(xp);
            long toNext = ExperienceUtils.getXpToNextLevel(xp);
            
            graphics.tooltip(
                    this.font,
                    List.of(
                        ClientTooltipComponent.create(Component.literal(hoveredSkill.getDisplayName() + ": " + lvl + "/99").getVisualOrderText()),
                        ClientTooltipComponent.create(Component.literal("Current Exp: " + xp).getVisualOrderText()),
                        ClientTooltipComponent.create(Component.literal("Exp to Level: " + (lvl >= 99 ? "MAX" : toNext)).getVisualOrderText())
                    ),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }
}