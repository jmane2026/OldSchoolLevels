package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Spell;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import com.jmane2026.oldschoollevels.network.SelectSpellPayload;
import java.util.stream.Collectors;

@EventBusSubscriber(value = Dist.CLIENT)
public class SpellScreen extends Screen {
    private static final int PANEL_WIDTH = 82; // Optimized for 3 columns
    private static final int PANEL_HEIGHT = 150;
    private static final int MARGIN = 10;
    private static final int ICON_SIZE = 18;
    private static final int SPACING = 4;

    public SpellScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("X"), (btn) -> this.onClose())
                .bounds(x + PANEL_WIDTH - 16, y + 2, 14, 14).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Background and panel must be drawn BEFORE super call so widgets appear on top
        renderSpellbookBackground(graphics);
        
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xCC000000);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.centeredText(this.font, this.title, x + PANEL_WIDTH / 2, y + 5, 0xFFFFFFFF);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        SkillData data = mc.player.getData(ModAttachments.SKILLS.get());
        int magicLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));
        Spell activeSpell = mc.player.getData(ModAttachments.ACTIVE_SPELL.get());

        int currentX = x + MARGIN;
        int currentY = y + 22;
        Spell hoveredSpell = null;

        for (Spell spell : Spell.values()) {
            boolean isMouseOver = mouseX >= currentX && mouseX < currentX + ICON_SIZE &&
                    mouseY >= currentY && mouseY < currentY + ICON_SIZE;

            boolean canCast = magicLevel >= spell.getRequiredMagicLevel();
            boolean hasCosts = spell.getCosts().stream().allMatch(c -> mc.player.getInventory().countItem(c.item().get()) >= c.amount());

            // 1. Highlight ACTIVE spell in Yellow
            if (spell.equals(activeSpell)) {
                graphics.fill(currentX - 1, currentY - 1, currentX + ICON_SIZE + 1, currentY + ICON_SIZE + 1, 0xFFFFD700);
            }

            // 2. Render Icon (Fixed UVs for no checkerboard)
            graphics.blit(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, spell.getIconTexture(),
                    currentX, currentY, 0.0f, 0.0f, ICON_SIZE, ICON_SIZE, 32, 32, 32, 32, -1);

            if (isMouseOver) {
                graphics.fill(currentX, currentY, currentX + ICON_SIZE, currentY + ICON_SIZE, 0x44FFFFFF);
                hoveredSpell = spell;
            }

            if (!canCast || !hasCosts) {
                graphics.fill(currentX, currentY, currentX + ICON_SIZE, currentY + ICON_SIZE, 0x99000000);
            }

            currentX += ICON_SIZE + SPACING;
            if (currentX + ICON_SIZE > x + PANEL_WIDTH - MARGIN) {
                currentX = x + MARGIN;
                currentY += ICON_SIZE + SPACING;
            }
        }

        if (hoveredSpell != null) {
            renderSpellTooltip(graphics, hoveredSpell, mouseX, mouseY, magicLevel, mc.player);
        }
    }

    private void renderSpellbookBackground(GuiGraphicsExtractor graphics) {
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xCC000000);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF);
    }

    private void renderSpellTooltip(GuiGraphicsExtractor graphics, Spell spell, int mouseX, int mouseY, int magicLevel, Player player) {
        List<Component> lines = new ArrayList<>();
        lines.add(spell.getNameComponent().copy().withStyle(net.minecraft.ChatFormatting.GOLD));
        lines.add(Component.literal("Required Magic: " + spell.getRequiredMagicLevel()).withStyle(
                magicLevel >= spell.getRequiredMagicLevel() ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));

        lines.add(Component.empty());
        lines.add(Component.empty());
        lines.add((Component.empty()));

        List<ClientTooltipComponent> components = lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create).collect(Collectors.toList());

        graphics.tooltip(this.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);

        // Render Sigil Icons manually inside the expanded tooltip area
        int iconX = mouseX + 15;
        int iconY = mouseY + 12; // Shifted up to fit inside the dark box

        for (Spell.SpellCost cost : spell.getCosts()) {
            int owned = player.getInventory().countItem(cost.item().get());
            boolean enough = owned >= cost.amount();

            graphics.item(new ItemStack(cost.item().get()), iconX, iconY);

            // Quantity logic: Show * if count > 64
            String ownedStr = owned > 64 ? "*" : String.valueOf(owned);
            String text = ownedStr + "/" + cost.amount();
            graphics.centeredText(this.font, Component.literal(text), iconX + 8, iconY + 18, enough ? 0xFF00FF00 : 0xFFFF0000);

            iconX += 35; // Increased spacing to prevent overlapping quantity text
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            Minecraft mc = this.minecraft;
            if (mc == null || mc.player == null) return super.mouseClicked(event, doubleClick);

            double mouseX = event.x();
            double mouseY = event.y();

            int x = (this.width - PANEL_WIDTH) / 2;
            int y = (this.height - PANEL_HEIGHT) / 2;
            int curX = x + MARGIN;
            int curY = y + 22;

            SkillData data = mc.player.getData(ModAttachments.SKILLS.get());
            int magicLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));

            for (Spell spell : Spell.values()) {
                if (mouseX >= curX && mouseX < curX + ICON_SIZE && mouseY >= curY && mouseY < curY + ICON_SIZE) {

                    boolean canCast = magicLevel >= spell.getRequiredMagicLevel();
                    boolean hasCosts = spell.getCosts().stream().allMatch(c ->
                            mc.player.getInventory().countItem(c.item().get()) >= c.amount());

                    if (canCast && hasCosts) {
                        ClientPacketDistributor.sendToServer(new SelectSpellPayload(spell));
                        mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
                        this.onClose();
                        return true;
                    }
                }
                curX += ICON_SIZE + SPACING;
                if (curX + ICON_SIZE > x + PANEL_WIDTH - MARGIN) {
                    curX = x + MARGIN;
                    curY += ICON_SIZE + SPACING;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
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
    public boolean isPauseScreen() { return false; }
}