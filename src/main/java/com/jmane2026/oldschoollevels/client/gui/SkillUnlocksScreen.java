package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent; // Added for explicit mouse event handling
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public class SkillUnlocksScreen extends Screen {
    private final Skill skill;
    private final Screen parent;
    private static final int WIDTH = 105;
    private static final int HEIGHT = 166;
    private static final int VISIBLE_ITEMS = 9;
    
    // STATIC STATE: Keeps scroll position while docked to inventory
    private static int staticScrollOffset = 0;
    private static boolean isDraggingScrollbar = false;

    public SkillUnlocksScreen(Skill skill, Screen parent) {
        super(Component.literal(skill.getDisplayName() + " Unlocks"));
        this.skill = skill;
        this.parent = parent;
    }

    public static void resetScroll() {
        staticScrollOffset = 0;
    }

    @Override
    protected void init() {
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;

        // "X" Close Button
        this.addRenderableWidget(Button.builder(Component.literal("X"), (_) -> this.onClose())
                .bounds(startX + WIDTH - 18, startY + 2, 14, 14).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
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
        handleScroll(scrollY);
        return true;
    }

    public static void handleScroll(double delta) {
        Skill skill = InventoryStyleOverlay.selectedSkill;
        if (skill == null) return;
        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int maxScroll = Math.max(0, unlocks.size() - VISIBLE_ITEMS);

        if (delta > 0) staticScrollOffset = Math.max(0, staticScrollOffset - 1);
        else if (delta < 0) staticScrollOffset = Math.min(maxScroll, staticScrollOffset + 1);
    }

    // MOUSE CLICK INPUT: Detects if click lands on the scrollbar track area
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        double mouseX = event.x();
        double mouseY = event.y();

        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        if (unlocks.size() > VISIBLE_ITEMS) {
            int startX = (this.width - WIDTH) / 2;
            int startY = (this.height - HEIGHT) / 2;

            int barX = startX + WIDTH - 8;
            int barY = startY + 22;
            int barHeight = HEIGHT - 30;

            // Check if click lands within the scroll track bounds (plus a little padding for easier clicking)
            if (mouseX >= barX - 2 && mouseX <= barX + 6 && mouseY >= barY && mouseY <= barY + barHeight) {
                isDraggingScrollbar = true;
                updateScrollFromMouseY(mouseY, skill, startY);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    // MOUSE RELEASE INPUT: Safely stops scroll drag tracking when mouse is let go
    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        isDraggingScrollbar = false;
        return super.mouseReleased(event);
    }

    // MOUSE DRAGGED PASSTHROUGH: Handles dynamic active dragging tracking

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (isDraggingScrollbar) {
            updateScrollFromMouseY(dy, skill, (this.height - HEIGHT) / 2);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    // SCROLL VALUE CALCULATOR: Converts raw vertical position to list index shifts
    private static void updateScrollFromMouseY(double mouseY, Skill skill, int startY) {
        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int maxScroll = Math.max(0, unlocks.size() - VISIBLE_ITEMS);
        if (maxScroll == 0) return;

        int barY = startY + 22; // Start higher to align with content
        int barHeight = HEIGHT - 30; // End before the bottom bezel
        int usableHeight = barHeight - 12; // Track height minus thumb slider node height

        // Normalize vertical movement percentage to a 0.0 - 1.0 spectrum scale
        double progress = (mouseY - barY - 6) / usableHeight;
        progress = Math.clamp(progress, 0.0, 1.0); // Clamp range limits

        staticScrollOffset = (int) Math.round(progress * maxScroll);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;
        renderOverlay(graphics, startX, startY, this.skill);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    public static void renderOverlay(GuiGraphicsExtractor graphics, int startX, int startY, Skill skill) {
        Minecraft mc = Minecraft.getInstance();
        if (skill == null) return;

        int mouseY = (int)(mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight());
        mouseY -= mc.screen instanceof InventoryScreen inv ? inv.getTopPos() : 0;

        if (isDraggingScrollbar) updateScrollFromMouseY(mouseY, skill, startY);

        // 1. Fill the main inner panel background with vanilla gray
        graphics.fill(startX, startY, startX + WIDTH, startY + HEIGHT, 0xFFC6C6C6);
        graphics.fill(startX, startY, startX + WIDTH, startY + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(startX, startY, startX + 1, startY + HEIGHT, 0xFFFFFFFF); // Left edge
        graphics.fill(startX, startY + HEIGHT - 1, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(startX + WIDTH - 1, startY, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Right edge

        int titleWidth = mc.font.width(skill.getDisplayName());
        graphics.text(mc.font, skill.getDisplayName(), startX + (WIDTH / 2) - (titleWidth / 2), startY + 10, 0xFF404040, false);

        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int yPos = startY + 28;
        float scale = 0.65f; // Even smaller for overlay

        // Render visible item based on scroll offset
        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            int idx = i + staticScrollOffset;
            if (idx >= unlocks.size()) break;

            RequirementUtils.UnlockInfo unlock = unlocks.get(idx);

            graphics.pose().pushMatrix();
            graphics.pose().translate((float)(startX + 5), (float)yPos, graphics.pose());
            graphics.pose().scale(scale, scale, graphics.pose());

            boolean renderedCustom = false;
            if (skill == Skill.MAGIC) {
                for (Spell spell : Spell.values()) {
                    if (unlock.description().equals(spell.getDisplayName())) {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, spell.getIconTexture(),
                                0, 0, 0.0f, 0.0f, 16, 16, 16, 16, 16, 16, -1);
                        renderedCustom = true;
                        break;
                    }
                }
            }

            if (!renderedCustom) {
                graphics.item(unlock.icon(), 0, 0);
            }

            String lvlStr = String.format("Lvl %02d:", unlock.level());
            graphics.text(mc.font, lvlStr, 22, 4, 0xFF404040, false);
            
            graphics.pose().pushMatrix();
            graphics.pose().translate(58, 4.5f, graphics.pose());
            graphics.pose().scale(0.65f, 0.65f, graphics.pose()); // Scale down to fit 105px width
            graphics.text(mc.font, unlock.description(), 0, 0, 0xFF0000AA, false);
            graphics.pose().popMatrix();

            graphics.pose().popMatrix();
            yPos += 16;
        }

        if (unlocks.size() > VISIBLE_ITEMS) {
            int barX = startX + WIDTH - 8; // Shifted inward slightly for better border padding
            int barY = startY + 25;
            int barHeight = HEIGHT - 35;

            graphics.fill(barX, barY, barX + 4, barY + barHeight, 0xFF373737);

            float progress = (float) staticScrollOffset / (unlocks.size() - VISIBLE_ITEMS);
            int thumbY = barY + (int)(progress * (barHeight - 12));

            graphics.fill(barX, thumbY, barX + 4, thumbY + 12, 0xFF8B8B8B);
            graphics.outline(barX, thumbY, 4, 12, 0xFF555555);
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override public boolean isPauseScreen() { return false; }
}