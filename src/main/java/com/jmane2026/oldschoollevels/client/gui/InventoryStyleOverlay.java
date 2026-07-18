package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.OSLConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class InventoryStyleOverlay {

    private enum DraggedButton { NONE, STATS, SKILLS, SPELLS }
    private static DraggedButton currentDragged = DraggedButton.NONE;
    private static double startMouseX, startMouseY;
    private static int startOffsetX, startOffsetY;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            Minecraft mc = Minecraft.getInstance();
            int x = inv.getLeftPos() + 77;
            int y = inv.getTopPos();

            // Stats Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) mc.setScreen(new CharacterStatsScreen());
                    }).bounds(x + OSLConfig.STATS_BUTTON_X.get(), y + 43 + OSLConfig.STATS_BUTTON_Y.get(), 16, 16)
                    .tooltip(Tooltip.create(Component.literal("Character Sheet (Shift+Drag to Move)"))).build());

            // Skills Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) mc.setScreen(new LevelScreen(Component.literal("Skills")));
                    }).bounds(x + OSLConfig.SKILLS_BUTTON_X.get(), y + 25 + OSLConfig.SKILLS_BUTTON_Y.get(), 16, 16)
                    .tooltip(Tooltip.create(Component.literal("Skills (Shift+Drag to Move)"))).build());

            // Spells Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) mc.setScreen(new SpellScreen(Component.literal("Spells")));
                    }).bounds(x + OSLConfig.SPELLS_BUTTON_X.get(), y + 7 + OSLConfig.SPELLS_BUTTON_Y.get(), 16, 16)
                    .tooltip(Tooltip.create(Component.literal("Spellbook (Shift+Drag to Move)"))).build());
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof InventoryScreen inv && isShiftDown() && event.getButton() == 0) {
            double mx = event.getMouseX();
            double my = event.getMouseY();
            int x = inv.getLeftPos() + 77;
            int y = inv.getTopPos();

            if (isMouseOver(mx, my, x + OSLConfig.STATS_BUTTON_X.get(), y + 43 + OSLConfig.STATS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.STATS_BUTTON_X.get(), OSLConfig.STATS_BUTTON_Y.get(), DraggedButton.STATS);
            } else if (isMouseOver(mx, my, x + OSLConfig.SKILLS_BUTTON_X.get(), y + 25 + OSLConfig.SKILLS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.SKILLS_BUTTON_X.get(), OSLConfig.SKILLS_BUTTON_Y.get(), DraggedButton.SKILLS);
            } else if (isMouseOver(mx, my, x + OSLConfig.SPELLS_BUTTON_X.get(), y + 7 + OSLConfig.SPELLS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.SPELLS_BUTTON_X.get(), OSLConfig.SPELLS_BUTTON_Y.get(), DraggedButton.SPELLS);
            }
        }
    }

    private static void grab(double mx, double my, int ox, int oy, DraggedButton button) {
        currentDragged = button;
        startMouseX = mx;
        startMouseY = my;
        startOffsetX = ox;
        startOffsetY = oy;
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (currentDragged != DraggedButton.NONE && event.getButton() == 0) {
            currentDragged = DraggedButton.NONE;
            OSLConfig.COMMON_SPEC.save(); // Save once drag is finished
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof InventoryScreen inv && currentDragged != DraggedButton.NONE) {
            // Absolute 1-1 calculation: Initial Offset + (Current Mouse - Start Mouse)
            int newX = startOffsetX + (int) Math.round(event.getMouseX() - startMouseX);
            int newY = startOffsetY + (int) Math.round(event.getMouseY() - startMouseY);

            switch (currentDragged) {
                case STATS -> { OSLConfig.STATS_BUTTON_X.set(newX); OSLConfig.STATS_BUTTON_Y.set(newY); }
                case SKILLS -> { OSLConfig.SKILLS_BUTTON_X.set(newX); OSLConfig.SKILLS_BUTTON_Y.set(newY); }
                case SPELLS -> { OSLConfig.SPELLS_BUTTON_X.set(newX); OSLConfig.SPELLS_BUTTON_Y.set(newY); }
            }

            // Hitbox update
            inv.init(inv.width, inv.height);
        }
    }

    @SubscribeEvent
    public static void onForegroundRender(net.neoforged.neoforge.client.event.ContainerScreenEvent.Render.Foreground event) {
        if (event.getContainerScreen() instanceof InventoryScreen) {
            // In the Foreground event, 0,0 is the top-left of the inventory box (leftPos, topPos)
            int x = 77;
            int y = 0;

            // Render STATS icon
            event.getGuiGraphics().item(new ItemStack(Items.NETHERITE_HELMET),
                    x + OSLConfig.STATS_BUTTON_X.get(), y + 43 + OSLConfig.STATS_BUTTON_Y.get());

            // Render SKILLS icon
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(
                    (x + OSLConfig.SKILLS_BUTTON_X.get() + 1.5f),
                    (y + 25 + OSLConfig.SKILLS_BUTTON_Y.get() + 1.5f),
                    event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.8f, 0.8f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.EXPERIENCE_BOTTLE), 0, 0);
            event.getGuiGraphics().pose().popMatrix();

            // Render SPELLS icon
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(
                    (float)(x + OSLConfig.SPELLS_BUTTON_X.get() + 2),
                    (float)(y + 7 + OSLConfig.SPELLS_BUTTON_Y.get() + 2),
                    event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.75f, 0.75f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.ENCHANTED_BOOK), 0, 0);
            event.getGuiGraphics().pose().popMatrix();
        }
    }

    private static boolean isShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }
}
