package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
    private static final int WIDTH = 170;
    private static final int HEIGHT = 180;
    private static final int VISIBLE_ITEMS = 9;
    private int scrollOffset = 0;

    // DRAG VARIABLES: Tracks active drag state
    private boolean isDraggingScrollbar = false;

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
        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int maxScroll = Math.max(0, unlocks.size() - VISIBLE_ITEMS);

        if (scrollY > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        else if (scrollY < 0) scrollOffset = Math.min(maxScroll, scrollOffset + 1);

        return true;
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
            int barY = startY + 28;
            int barHeight = VISIBLE_ITEMS * 16;

            // Check if click lands within the scroll track bounds (plus a little padding for easier clicking)
            if (mouseX >= barX - 2 && mouseX <= barX + 6 && mouseY >= barY && mouseY <= barY + barHeight) {
                this.isDraggingScrollbar = true;
                updateScrollFromMouseY(mouseY);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    // MOUSE RELEASE INPUT: Safely stops scroll drag tracking when mouse is let go
    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        this.isDraggingScrollbar = false;
        return super.mouseReleased(event);
    }

    // MOUSE DRAGGED PASSTHROUGH: Handles dynamic active dragging tracking

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (this.isDraggingScrollbar) {
            updateScrollFromMouseY(dy);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    // SCROLL VALUE CALCULATOR: Converts raw vertical position to list index shifts
    private void updateScrollFromMouseY(double mouseY) {
        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int maxScroll = Math.max(0, unlocks.size() - VISIBLE_ITEMS);
        if (maxScroll == 0) return;

        int startY = (this.height - HEIGHT) / 2;
        int barY = startY + 28;
        int barHeight = VISIBLE_ITEMS * 16;
        int usableHeight = barHeight - 12; // Track height minus thumb slider node height

        // Normalize vertical movement percentage to a 0.0 - 1.0 spectrum scale
        double progress = (mouseY - barY - 6) / usableHeight;
        progress = Math.clamp(progress, 0.0, 1.0); // Clamp range limits

        this.scrollOffset = (int) Math.round(progress * maxScroll);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // CONTINUOUS INTERACTIVE CAPTURE: Keeps track of movement if API bypasses default mouseDragged triggers
        if (this.isDraggingScrollbar) {
            updateScrollFromMouseY(mouseY);
        }

        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;

        // 1. Fill the main inner panel background with vanilla gray
        graphics.fill(startX, startY, startX + WIDTH, startY + HEIGHT, 0xFFC6C6C6);

        // 2. Draw the white highlight lines (Top and Left borders)
        graphics.fill(startX, startY, startX + WIDTH, startY + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(startX, startY, startX + 1, startY + HEIGHT, 0xFFFFFFFF); // Left edge

        // 3. Draw the dark gray shadow lines (Bottom and Right borders)
        graphics.fill(startX, startY + HEIGHT - 1, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(startX + WIDTH - 1, startY, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Right edge

        // Title - Converted to drawString with NO drop shadow and dark gray text
        int titleWidth = this.font.width(this.title);
        graphics.text(this.font, this.title, startX + (WIDTH / 2) - (titleWidth / 2), startY + 10, 0xFF404040, false);

        List<RequirementUtils.UnlockInfo> unlocks = RequirementUtils.getUnlocksForSkill(skill);
        int yPos = startY + 28;
        float scale = 0.85f; // Scale factor for items and text

        // Render visible item based on scroll offset
        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            int idx = i + scrollOffset;
            if (idx >= unlocks.size()) break;

            RequirementUtils.UnlockInfo unlock = unlocks.get(idx);

            graphics.pose().pushMatrix();
            graphics.pose().translate((float)(startX + 8), (float)yPos, graphics.pose());
            graphics.pose().scale(scale, scale, graphics.pose());

            // Logic to render actual Spell PNGs if this is the Magic skill
            boolean renderedCustom = false;
            if (this.skill == Skill.MAGIC) {
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

            // LEVEL STRING: Changed from white to a dark gray with NO shadow
            String lvlStr = String.format("Lvl %02d:", unlock.level());
            graphics.text(this.font, lvlStr, 22, 4, 0xFF404040, false);

            // UNLOCK DESCRIPTION: Changed from light gray to dark blue/indigo with NO shadow for excellent contrast
            graphics.text(this.font, unlock.description(), 58, 4, 0xFF0000AA, false);

            graphics.pose().popMatrix();
            yPos += 16;
        }

        // 4. Vanilla Style Scrollbar Track (Darker inset rail, gray thumb button)
        if (unlocks.size() > VISIBLE_ITEMS) {
            int barX = startX + WIDTH - 8; // Shifted inward slightly for better border padding
            int barY = startY + 28;
            int barHeight = VISIBLE_ITEMS * 16;

            // Dark sunken background track slot (Replicating vanilla item slot tones)
            graphics.fill(barX, barY, barX + 4, barY + barHeight, 0xFF373737);

            float progress = (float) scrollOffset / (unlocks.size() - VISIBLE_ITEMS);
            int thumbY = barY + (int)(progress * (barHeight - 12));

            // Solid gray scroll thumb slider panel with standard dark outline shadow
            graphics.fill(barX, thumbY, barX + 4, thumbY + 12, 0xFF8B8B8B);
            graphics.outline(barX, thumbY, 4, 12, 0xFF555555);
        }

        // RENDER SUPER LAST: Ensures close buttons, search fields, or sliders render on top cleanly
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override public boolean isPauseScreen() { return false; }
}