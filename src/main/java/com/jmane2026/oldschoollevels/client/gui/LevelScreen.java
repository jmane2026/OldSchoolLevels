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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class LevelScreen extends Screen {
    private static final int PANEL_WIDTH = 105; // Widened for 4 columns
    private static final int PANEL_HEIGHT = 166; // Matches inventory height
    private static final int BOX_SIZE = 28;
    private static final int SPACING = 1; // Tighter spacing for 4-wide
    private static final int PADDING = 6;
    private static final int MARGIN = 10;

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

    public static void renderOverlay(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        // Mouse relative to the panel's top-left (x, y) for accurate hit detection
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        // 1. Fill the main inner panel background with vanilla gray
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFFC6C6C6);

        // 2. Draw the white highlight lines (Top and Left borders)
        graphics.fill(x, y, x + PANEL_WIDTH, y + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(x, y, x + 1, y + PANEL_HEIGHT, 0xFFFFFFFF); // Left edge

        // 3. Draw the dark gray shadow lines (Bottom and Right borders)
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF555555); // Right edge

        // Title - Converted to drawString with NO drop shadow and dark gray text
        int titleWidth = mc.font.width("Skills");
        graphics.text(mc.font, "Skills", x + (PANEL_WIDTH / 2) - (titleWidth / 2), y + 5, 0xFF404040, false);

        int startX = x + PADDING;
        int startY = y + 18;
        int totalLevel = 0;
        Skill hoveredSkill = null;

        // Scale the entire grid down to fit the smaller panel
        graphics.pose().pushMatrix();
        graphics.pose().translate(startX, startY, graphics.pose());
        graphics.pose().scale(0.8f, 0.8f, graphics.pose());

        // Temporary variables for Combat Level calculation
        int atk = 1, def = 1, str = 1, hp = 10, range = 1;
        double pray = 1, mage = 1;

        // Fetch the player's real skill data from Attachments
        SkillData skillData = mc.player != null
                ? mc.player.getData(ModAttachments.SKILLS)
                : SkillData.EMPTY;

        Skill[] skills = Skill.values();
        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            int col = i % 4; // Shifted to 4 columns
            int row = i / 4;
            int bx = (col * (BOX_SIZE + SPACING));
            int by = (row * (BOX_SIZE + SPACING));

            // Draw Skill Box - Solid, opaque darker panel background with a subtle border match
            graphics.fill(bx, by, bx + BOX_SIZE, by + BOX_SIZE, 0xFF8B8B8B);
            graphics.outline(bx, by, BOX_SIZE, BOX_SIZE, 0xFF373737); // Dark gray inset border look

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
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, skill.getSpriteIcon(), bx + (BOX_SIZE / 2) - 8, by + 2, 16, 16);
            } else {
                graphics.item(skill.getIcon(), bx + (BOX_SIZE / 2) - 8, by + 2);
            }

            // Render Level Text - Adjusted to a readable yellow/gold with NO drop shadow
            String lvlStr = level + "/99";
            graphics.pose().pushMatrix();
            graphics.pose().translate(bx + (BOX_SIZE / 2f), by + 20f, graphics.pose());
            graphics.pose().scale(0.4f, 0.4f, graphics.pose()); // scaled to fit 4-wide boxes
            graphics.text(mc.font, lvlStr, (int) (-(mc.font.width(lvlStr) / 2f)), 0, 0xFFFFFF00, false);
            graphics.pose().popMatrix();

            // Check Hover (Relative Mouse vs. Padded Grid Coordinates)
            if (relMouseX >= PADDING + bx * 0.8f && relMouseX <= PADDING + (bx + BOX_SIZE) * 0.8f &&
                    relMouseY >= 18 + by * 0.8f && relMouseY <= 18 + (by + BOX_SIZE) * 0.8f) {
                graphics.fill(bx, by, bx + BOX_SIZE, by + BOX_SIZE, 0x22FFFFFF); // Translucent hover panel
                hoveredSkill = skill;
            }
        }
        graphics.pose().popMatrix();

        // Combat Level Calculation (OSRS Formula)
        double base = 0.25 * (def + hp + Math.floor(pray / 2));
        double melee = 0.325 * (atk + str);
        double ranged = 0.325 * Math.floor(3 * range / 2.0);
        double magic = 0.325 * Math.floor(3 * mage / 2.0);
        int combatLevel = (int) (base + Math.max(melee, Math.max(ranged, magic)));

        // Summary Info (Bottom area of the panel)
        String combatText = "Combat Level: " + combatLevel;
        String totalText = "Total Level: " + totalLevel;
        int ty = y + PANEL_HEIGHT - 11;

        // Total Level (Bottom Left)
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + PADDING, ty, graphics.pose());
        graphics.pose().scale(0.7f, 0.7f, graphics.pose());
        graphics.text(mc.font, totalText, 0, 0, 0xFFD47A00, false);
        graphics.pose().popMatrix();

        // Combat Level (Stacked above Total Level)
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + PADDING, ty - 8, graphics.pose());
        graphics.pose().scale(0.7f, 0.7f, graphics.pose()); // Combat is slightly smaller secondary info
        graphics.text(mc.font, combatText, 0, 0, 0xFFD47A00, false);
        graphics.pose().popMatrix();


        // Tooltip Rendering
        if (hoveredSkill != null) {
            long xp = skillData.getExperience(hoveredSkill);
            int lvl = ExperienceUtils.getLevelAtExperience(xp);
            long toNext = ExperienceUtils.getXpToNextLevel(xp);
            boolean clickable = hoveredSkill != Skill.STRENGTH && hoveredSkill != Skill.LIFE;

            graphics.tooltip(
                    mc.font,
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

    public static void handleOverlayClick(double mx, double my, int x, int y) {
        int gridX = x + PADDING;
        int gridY = y + 18;
        Skill[] skills = Skill.values();
        for (int i = 0; i < skills.length; i++) {
            int col = i % 4; // Sync with 4-column render
            int row = i / 4;
            // Account for 0.8x scale used in rendering the grid
            float bx = gridX + (col * (BOX_SIZE + SPACING)) * 0.8f;
            float by = gridY + (row * (BOX_SIZE + SPACING)) * 0.8f;

            if (mx >= bx && mx <= bx + (BOX_SIZE * 0.8f) && my >= by && my <= by + (BOX_SIZE * 0.8f)) {
                Skill selected = skills[i];
                if (selected == Skill.STRENGTH || selected == Skill.LIFE) return;
                
                // Play UI Click Sound
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
                }

                InventoryStyleOverlay.selectedSkill = selected;
                InventoryStyleOverlay.activePanel = InventoryStyleOverlay.Panel.UNLOCKS;
                return;
            }
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
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