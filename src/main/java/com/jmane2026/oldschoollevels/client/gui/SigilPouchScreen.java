package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import com.jmane2026.oldschoollevels.common.menus.SigilPouchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class SigilPouchScreen extends AbstractContainerScreen<SigilPouchMenu> {
    private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final int POUCH_WIDTH = 90;
    private static final int POUCH_HEIGHT = 84;

    public SigilPouchScreen(SigilPouchMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int xm, int ym) {
        // Leaving this empty to override the base container title drawing behavior
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. Render ONLY the bottom part of the inventory starting at V=79 to hide the armor slots
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y + 79, 0.0f, 79.0f, 176, 87, 256, 256);

        // 2. Setup OSRS Pouch Panel Dimensions
        int pouchX = x - POUCH_WIDTH - 2;
        int pouchY = y + 79; // Aligned flush with the inventory background crop

        // 3. Fill the pouch panel with vanilla gray background matching the inventory texture
        graphics.fill(pouchX, pouchY, pouchX + POUCH_WIDTH, pouchY + POUCH_HEIGHT, 0xFFC6C6C6);

        // 4. Draw the white highlight lines (Top and Left borders)
        graphics.fill(pouchX, pouchY, pouchX + POUCH_WIDTH, pouchY + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(pouchX, pouchY, pouchX + 1, pouchY + POUCH_HEIGHT, 0xFFFFFFFF); // Left edge

        // 5. Draw the dark gray shadow lines (Bottom and Right borders)
        graphics.fill(pouchX, pouchY + POUCH_HEIGHT - 1, pouchX + POUCH_WIDTH, pouchY + POUCH_HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(pouchX + POUCH_WIDTH - 1, pouchY, pouchX + POUCH_WIDTH, pouchY + POUCH_HEIGHT, 0xFF555555); // Right edge

        // Title - Converted to clean charcoal color with no blurry drop-shadow
        int titleWidth = this.font.width("Sigil Pouch");
        graphics.text(this.font, "Sigil Pouch", pouchX + (POUCH_WIDTH / 2) - (titleWidth / 2), pouchY - 10, 0xFF404040, false);

        // 6. Render Sigil counts inside the pouch panel
        int startX = pouchX + 5;
        int startY = pouchY + 5;

        int sigilCount = this.menu.getSigilTypes().size();
        for (int i = 0; i < sigilCount; i++) {
            int slotX = startX + (i % 3 * 26);
            int slotY = startY + (i / 3 * 26);

            // Draw a subtle sunken item slot background for each sigil slot to mimic standard inventory panels
            graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF8B8B8B);
            graphics.outline(slotX - 1, slotY - 1, 18, 18, 0xFF373737);

            Item sigil = this.menu.getSigilTypes().get(i);
            int count = SigilPouchItem.getSigilCount(this.menu.getPouch(), sigil);

            // Create item stack state reference
            ItemStack stack = new ItemStack(sigil);

            // Render Icon (Directly at slot coordinates for perfect alignment)
            graphics.fakeItem(stack, slotX, slotY);

            // Render text stack value numbers natively with standard built-in text styling
            if (count > 0) {
                String countStr = count > 999 ? "999+" : String.valueOf(count);
                int textX = slotX + 17 - font.width(countStr);
                int textY = slotY + 9;

                // Draw the clean item text overlay (White text with a true dark drop shadow)
                graphics.text(this.font, countStr, textX, textY, 0xFFFFFFFF, true);
            }
        }
    }
}
