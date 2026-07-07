package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.ChangeStylePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class InventoryStyleOverlay {
    private static boolean isDropdownOpen = false;
    private static final int DROPDOWN_WIDTH = 160;
    private static final int ITEM_HEIGHT = 20;
    private static Button triggerButton;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            isDropdownOpen = false; // Reset state when opening inventory
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Use the IAttachmentHolder interface directly to avoid ambiguity errors
            IAttachmentHolder holder = (IAttachmentHolder) mc.player;
            CombatStyle current = holder.getData(ModAttachments.COMBAT_STYLE.get());

            // The trigger button that toggles the list
            triggerButton = Button.builder(Component.literal("Style: ").append(current.getName()), (btn) -> {
                        isDropdownOpen = !isDropdownOpen;
                    })
                    .bounds(inv.getGuiLeft(), inv.getGuiTop() - 25, DROPDOWN_WIDTH, 20)
                    .build();

            event.addListener(triggerButton);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (isDropdownOpen && event.getScreen() instanceof InventoryScreen inv) {
            GuiGraphicsExtractor graphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();
            int x = inv.getGuiLeft();
            int y = inv.getGuiTop() - 5; // Start just below the trigger button

            CombatStyle[] styles = CombatStyle.values();
            int height = styles.length * ITEM_HEIGHT;

            // Draw Dropdown Background
            graphics.fill(x, y, x + DROPDOWN_WIDTH, y + height, 0xFF202020);
            graphics.outline(x, y, DROPDOWN_WIDTH, height, 0xFFFFFFFF);

            for (int i = 0; i < styles.length; i++) {
                int itemY = y + (i * ITEM_HEIGHT);
                boolean hovered = event.getMouseX() >= x && event.getMouseX() <= x + DROPDOWN_WIDTH
                        && event.getMouseY() >= itemY && event.getMouseY() <= itemY + ITEM_HEIGHT;

                graphics.text(mc.font, styles[i].getName(), x + 5, itemY + 6, hovered ? 0xFFFFFFA0 : 0xFFFFFFFF);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (isDropdownOpen && event.getScreen() instanceof InventoryScreen inv) {
            MouseButtonEvent mouse = event.getMouseButtonEvent();
            int x = inv.getGuiLeft();
            int y = inv.getGuiTop() - 5;

            CombatStyle[] styles = CombatStyle.values();

            // Check if clicking inside the dropdown area
            if (mouse.x() >= x && mouse.x() <= x + DROPDOWN_WIDTH
                    && mouse.y() >= y && mouse.y() <= y + (styles.length * ITEM_HEIGHT)) {

                int clickedIndex = (int)((mouse.y() - y) / ITEM_HEIGHT);
                if (clickedIndex >= 0 && clickedIndex < styles.length) {
                    CombatStyle selected = styles[clickedIndex];
                    Minecraft mc = Minecraft.getInstance();
                    IAttachmentHolder holder = (IAttachmentHolder) mc.player;

                    // Set Data & Sync
                    holder.setData(ModAttachments.COMBAT_STYLE.get(), selected);
                    ClientPacketDistributor.sendToServer(new ChangeStylePayload(selected));
                    
                    // Update the trigger button text
                    if (triggerButton != null) {
                        triggerButton.setMessage(Component.literal("Style: ").append(selected.getName()));
                    }

                    // Play click sound
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                    // Close and consume event
                    isDropdownOpen = false;
                    event.setCanceled(true);
                    return;
                }
            }

            // If we click anywhere else, close the dropdown
            if (!(mouse.x() >= x && mouse.x() <= x + DROPDOWN_WIDTH
                    && mouse.y() >= inv.getGuiTop() - 25 && mouse.y() <= inv.getGuiTop() - 5)) {
                isDropdownOpen = false;
            }
        }
    }
}