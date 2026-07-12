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

public class SigilPouchScreen extends AbstractContainerScreen<SigilPouchMenu> {
    private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final int POUCH_WIDTH = 90;
    private static final int POUCH_HEIGHT = 84;

    public SigilPouchScreen(SigilPouchMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {

    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. Render ONLY the bottom part of the inventory starting at V=79 to hide the boot slot
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y + 79, 0.0f, 79.0f, 176, 87, 256, 256);

        // 3. Render OSRS Pouch Panel. It starts slightly higher than the crop (y + 72) to fit the title
        int pouchX = x - POUCH_WIDTH - 2;
        int pouchY = y + 79; // Aligned flush with the inventory background crop
        graphics.fill(pouchX, pouchY, pouchX + POUCH_WIDTH, pouchY + POUCH_HEIGHT, 0xEE111115);
        graphics.outline(pouchX, pouchY, POUCH_WIDTH, POUCH_HEIGHT, 0xFFFFFFFF);
        // Title rendered just above the box to prevent icon overlap
        graphics.centeredText(this.font, "Sigil Pouch", pouchX + POUCH_WIDTH / 2, pouchY - 10, 0xFFFFAA00);

        // 4. Render Sigil counts inside the pouch panel
        int startX = pouchX + 5;
        // startY lands at y + 84 (79 + 5) to align exactly with the first row of inventory slots
        int startY = pouchY + 5; 
        
        int sigilCount = this.menu.getSigilTypes().size();
        for (int i = 0; i < sigilCount; i++) {
            int slotX = startX + (i % 3 * 26);
            int slotY = startY + (i / 3 * 26);

            Item sigil = this.menu.getSigilTypes().get(i);
            int count = SigilPouchItem.getSigilCount(this.menu.getPouch(), sigil);

            // Render Icon (Directly at slot coordinates for perfect alignment)
            graphics.fakeItem(new ItemStack(sigil), slotX, slotY);
            
            String countStr = count > 99 ? "*" : String.valueOf(count);
            // Outline logic for readability
            int textX = slotX + 16 - font.width(countStr);
            int textY = slotY + 8;
            graphics.text(this.font, countStr, textX + 1, textY, 0xFF000000);
            graphics.text(this.font, countStr, textX, textY, 0xFFFFFFFF);
        }
    }
}