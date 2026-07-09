package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public class SkillUnlocksScreen extends Screen {
    private final Skill skill;
    private final Screen parent;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 220;
    private static final int VISIBLE_ITEMS = 8;
    private int scrollOffset = 0;

    public SkillUnlocksScreen(Skill skill, Screen parent) {
        super(Component.literal(skill.getDisplayName() + " Unlocks"));
        this.skill = skill;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;

        // "X" Close Button
        this.addRenderableWidget(Button.builder(Component.literal("X"), (btn) -> this.onClose())
                .bounds(startX + WIDTH - 18, startY + 2, 14, 14).build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int maxScroll = Math.max(0, unlocks.size() - VISIBLE_ITEMS);
        
        if (scrollY > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        else if (scrollY < 0) scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;

        // Draw Background (widgets render behind due to super call order)
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.fill(startX, startY, startX + WIDTH, startY + HEIGHT, 0xEE111111);
        graphics.outline(startX, startY, WIDTH, HEIGHT, 0xFFFFFFFF);
        graphics.text(this.font, this.title, startX + (WIDTH / 2) - (font.width(this.title) / 2), startY + 10, 0xFFFFAA00);

        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int yPos = startY + 30;

        // Render visible item based on scroll offset
        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            int idx = i + scrollOffset;
            if (idx >= unlocks.size()) break;

            RequirementUtils.UnlockInfo unlock = unlocks.get(idx);
            graphics.item(unlock.icon(), startX + 5, yPos);
            String lvlStr = String.format("Lvl %02d:", unlock.level());
            
            graphics.text(this.font, lvlStr, startX + 25, yPos + 4, 0xFFFFFFFF);
            graphics.text(this.font, unlock.description(), startX + 62, yPos + 4, 0xFFBBBBBB);
            yPos += 22;
        }

        // Simple Scrollbar Track
        if (unlocks.size() > VISIBLE_ITEMS) {
            int barX = startX + WIDTH - 6;
            int barY = startY + 30;
            int barHeight = VISIBLE_ITEMS * 22;
            graphics.fill(barX, barY, barX + 2, barY + barHeight, 0xFF333333);
            float progress = (float) scrollOffset / (unlocks.size() - VISIBLE_ITEMS);
            int thumbY = barY + (int)(progress * (barHeight - 10));
            graphics.fill(barX, thumbY, barX + 2, thumbY + 10, 0xFFFFFFFF);
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}