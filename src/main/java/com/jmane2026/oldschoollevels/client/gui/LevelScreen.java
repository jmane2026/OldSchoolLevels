package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class LevelScreen extends Screen {
    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 245;
    private static final int MARGIN = 10;
    private static final int BOX_SIZE = 38;
    private static final int SPACING = 3;

    public LevelScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.width - PANEL_WIDTH - MARGIN;
        int y = this.height - PANEL_HEIGHT - MARGIN;

        // Small "X" button in the top right corner
        this.addRenderableWidget(Button.builder(Component.literal("X"), (_) -> this.onClose())
                .bounds(x + PANEL_WIDTH - 18, y + 2, 14, 14)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Call super first to ensure the base screen state (background/blur) is handled before our custom UI
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Calculate bottom right position
        int x = this.width - PANEL_WIDTH - MARGIN;
        int y = this.height - PANEL_HEIGHT - MARGIN;

        // Draw main panel box (OSRS style dark gray background)
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xAA000000);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF); // Solid White Outline

        // Title
        graphics.text(this.font, this.title, x + (PANEL_WIDTH / 2) - (this.font.width(this.title) / 2), y + 5, 0xFFFFFFFF);

        int startX = x + 20; // Increased padding to center the grid
        int startY = y + 20; // Pushed down to clear the title/X button
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
            if (skill == Skill.MAGIC) mage = level;

            // Render Icon (Sprite or Item)
            if (skill.getSpriteIcon() != null) {
                if (skill == Skill.MAGIC) {
                    // Calculate the current animation frame (0-31)
                    long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
                    int frameIndex = (int) ((time / 2) % 32); // Change frame every 2 ticks
                    int vOffset = frameIndex * 16;

                    graphics.blit(RenderPipelines.GUI_TEXTURED, skill.getSpriteIcon(), bx + (BOX_SIZE / 2) - 8, by + 2, 0, vOffset, 16, 16, 16, 16, 16, 512, -1);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, skill.getSpriteIcon(), bx + (BOX_SIZE / 2) - 8, by + 2, 16, 16);
                }
            } else {
                graphics.item(skill.getIcon(), bx + (BOX_SIZE / 2) - 8, by + 2);
            }

            // Render Level Text
            String lvlStr = "Lvl: " + level + "/99";
            graphics.pose().pushMatrix();
            // Move to the horizontal center of the box and the desired Y position
            graphics.pose().translate(bx + (BOX_SIZE / 2f), by + 28f, graphics.pose());
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
        graphics.text(this.font, totalText, (-this.font.width(totalText)), 0, 0xFFFFAA00);
        graphics.pose().popMatrix();

        // Tooltip Rendering
        if (hoveredSkill != null) {
            long xp = skillData.getExperience(hoveredSkill);
            int lvl = ExperienceUtils.getLevelAtExperience(xp);
            long toNext = ExperienceUtils.getXpToNextLevel(xp);
            boolean clickable = hoveredSkill != Skill.STRENGTH && hoveredSkill != Skill.LIFE;
            
            graphics.tooltip(
                    this.font,
                    List.of(
                        ClientTooltipComponent.create(Component.literal(hoveredSkill.getDisplayName() + ": " + lvl + "/99").getVisualOrderText()),
                        ClientTooltipComponent.create(Component.literal("Current Exp: " + xp).getVisualOrderText()),
                        ClientTooltipComponent.create(Component.literal("Exp to Level: " + (lvl >= 99 ? "MAX" : toNext)).getVisualOrderText()),
                        ClientTooltipComponent.create(Component.literal(clickable ? "§eClick to view Unlocks" : "§7No Unlocks to display").getVisualOrderText())
                    ),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Allow closing the screen with standard inventory key (E) or Escape
        if (event.key() == GLFW.GLFW_KEY_E || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        double mouseX = event.x();
        double mouseY = event.y();

        int x = this.width - PANEL_WIDTH - MARGIN;
        int y = this.height - PANEL_HEIGHT - MARGIN;
        int startX = x + 20;
        int startY = y + 20;

        Skill[] skills = Skill.values();
        for (int i = 0; i < skills.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int bx = startX + (col * (BOX_SIZE + SPACING));
            int by = startY + (row * (BOX_SIZE + SPACING));

            // Check if the click is within the current skill box bounds
            if (mouseX >= bx && mouseX <= bx + BOX_SIZE && mouseY >= by && mouseY <= by + BOX_SIZE) {
                Skill selected = skills[i];
                if (selected == Skill.STRENGTH || selected == Skill.LIFE) return false;

                this.minecraft.setScreen(new SkillUnlocksScreen(skills[i], this));
                return true;
            }
        }

        return super.mouseClicked(event, doubleClicked);
    }
}