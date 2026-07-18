package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Spell;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import com.jmane2026.oldschoollevels.network.SelectSpellPayload;
import java.util.stream.Collectors;

@EventBusSubscriber(value = Dist.CLIENT)
public class SpellScreen extends Screen {
    private static final int PANEL_WIDTH = 85;
    private static final int PANEL_HEIGHT = 166;
    private static final int MARGIN = 10;
    private static final int ICON_SIZE = 18;
    private static final int SPACING = 4;

    private static boolean canCast(int level, Spell spell)
    {
        return level >= spell.getRequiredMagicLevel();
    }

    private static boolean hasCosts(Spell spell, Minecraft mc, ItemStack finalPouch)
    {
        return spell.getCosts().stream().allMatch(c -> {
            assert mc.player != null;
            int owned = mc.player.getInventory().countItem(c.item().get());
            if (!finalPouch.isEmpty()) {
                owned += SigilPouchItem.getSigilCount(finalPouch, c.item().get());
            }
            return owned >= c.amount();
        });
    }

    public SpellScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("X"), (_) -> this.onClose())
                .bounds(x + PANEL_WIDTH - 16, y + 2, 14, 14).build());
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        renderOverlay(graphics, x, y, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    public static void renderOverlay(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        int relMouseX = mouseX - (mc.screen instanceof InventoryScreen inv ? inv.getLeftPos() : 0);
        int relMouseY = mouseY - (mc.screen instanceof InventoryScreen inv ? inv.getTopPos() : 0);

        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.fill(x, y, x + PANEL_WIDTH, y + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(x, y, x + 1, y + PANEL_HEIGHT, 0xFFFFFFFF); // Left edge
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xFF555555); // Right edge
        assert mc.player != null;
        SkillData data = mc.player.getData(ModAttachments.SKILLS.get());
        int magicLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));
        Spell activeSpell = mc.player.getData(ModAttachments.ACTIVE_SPELL.get());

        // Find sigil pouch once per frame for cost checks
        ItemStack pouch = ItemStack.EMPTY;
        for (ItemStack s : mc.player.getInventory().getNonEquipmentItems()) {
            if (s.getItem() instanceof SigilPouchItem) {
                pouch = s;
                break;
            }
        }
        
        int textWidth = mc.font.width("Spells");
        graphics.text(mc.font, "Spells", (x + PANEL_WIDTH / 2) - (textWidth / 2), y + 5, 0xFF404040, false);

        int currentX = x + 12; // Adjusted for 105 width
        int currentY = y + 22;
        Spell hoveredSpell = null;

        for (Spell spell : Spell.values()) {
            boolean isMouseOver = relMouseX >= currentX && relMouseX < currentX + ICON_SIZE &&
                    relMouseY >= currentY && relMouseY < currentY + ICON_SIZE;

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

            currentX += ICON_SIZE + SPACING;
            if (currentX + ICON_SIZE > x + PANEL_WIDTH - MARGIN) {
                currentX = x + 12;
                currentY += ICON_SIZE + SPACING;
            }
        }

        if (hoveredSpell != null) {
            renderSpellTooltip(graphics, mc, hoveredSpell, mouseX, mouseY, magicLevel, mc.player, pouch);
        }
    }

    public static void handleOverlayClick(double mx, double my, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int curX = x + 12;
        int curY = y + 22;

        ItemStack pouch = ItemStack.EMPTY;
        for (ItemStack s : mc.player.getInventory().getNonEquipmentItems()) {
            if (s.getItem() instanceof SigilPouchItem) {
                pouch = s;
                break;
            }
        }

        SkillData data = mc.player.getData(ModAttachments.SKILLS.get());
        int magicLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));

        for (Spell spell : Spell.values()) {
            if (mx >= curX && mx < curX + ICON_SIZE && my >= curY && my < curY + ICON_SIZE) {
                if (canCast(magicLevel, spell) && hasCosts(spell, mc, pouch)) {
                    ClientPacketDistributor.sendToServer(new SelectSpellPayload(spell));
                    assert mc.level != null;
                    mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
                    InventoryStyleOverlay.activePanel = InventoryStyleOverlay.Panel.NONE;
                    return;
                }
            }
            curX += ICON_SIZE + SPACING;
            if (curX + ICON_SIZE > x + PANEL_WIDTH - MARGIN) {
                curX = x + 12;
                curY += ICON_SIZE + SPACING;
            }
        }
    }

    private static void renderSpellTooltip(GuiGraphicsExtractor graphics, Minecraft mc, Spell spell, int mouseX, int mouseY, int magicLevel, Player player, ItemStack pouch) {
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

        graphics.pose().pushMatrix();
        graphics.pose().identity();

        // Calculate actual tooltip width to sync with Minecraft's internal flipping logic
        int tooltipWidth = 0;
        for (ClientTooltipComponent component : components) {
            tooltipWidth = Math.max(tooltipWidth, component.getWidth(mc.font));
        }
        // Ensure width accounts for the row of sigil icons (35px per icon)
        tooltipWidth = Math.max(tooltipWidth, spell.getCosts().size() * 35);

        // Standard Minecraft tooltip flip logic: if the box exceeds screen width, it renders to the left of the mouse
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipWidth > mc.getWindow().getGuiScaledWidth()) {
            tooltipX = mouseX - 16 - tooltipWidth;
        }

        graphics.tooltip(mc.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);

        int iconX = tooltipX + 5; // Anchor icons to the actual start of the rendered black box
        int iconY = mouseY + 12;

        for (Spell.SpellCost cost : spell.getCosts()) {
            int owned = player.getInventory().countItem(cost.item().get());
            if (!pouch.isEmpty()) {
                owned += SigilPouchItem.getSigilCount(pouch, cost.item().get());
            }
            
            boolean enough = owned >= cost.amount();

            graphics.item(new ItemStack(cost.item().get()), iconX, iconY);

            // Quantity logic: Show * if count > 64
            String ownedStr = owned > 64 ? "*" : String.valueOf(owned);
            String text = ownedStr + "/" + cost.amount();
            graphics.centeredText(mc.font, Component.literal(text), iconX + 8, iconY + 18, enough ? 0xFF00FF00 : 0xFFFF0000);

            iconX += 35; // Increased spacing to prevent overlapping quantity text
        }
        graphics.pose().popMatrix();
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
        if (this.minecraft.player != null) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }
}