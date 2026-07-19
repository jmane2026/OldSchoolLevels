package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.OSLConfig;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.ChangeStylePayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import java.util.List;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class InventoryStyleOverlay {

    public enum Panel { NONE, STATS, SKILLS, SPELLS, UNLOCKS }
    public static Panel activePanel = Panel.NONE;
    public static Skill selectedSkill = null; // Used for the Unlocks panel

    private enum DraggedButton { NONE, STATS, SKILLS, SPELLS }
    private static DraggedButton currentDragged = DraggedButton.NONE;
    private static double startMouseX, startMouseY;
    private static int startOffsetX, startOffsetY;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            // Use the base (centered) X to ensure hitboxes stay stationary regardless of recipe book
            int x = getBaseX(inv) + 77;
            int y = inv.getTopPos(); // Y doesn't shift with the recipe book

            // Stats Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) togglePanel(Panel.STATS);
                    }).bounds(x + OSLConfig.STATS_BUTTON_X.get(), y + 41 + OSLConfig.STATS_BUTTON_Y.get(), 16, 16).build());

            // Skills Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) togglePanel(Panel.SKILLS);
                    }).bounds(x + OSLConfig.SKILLS_BUTTON_X.get(), y + 23 + OSLConfig.SKILLS_BUTTON_Y.get(), 16, 16).build());

            // Spells Button
            event.addListener(Button.builder(Component.empty(), (_) -> {
                        if (!isShiftDown()) togglePanel(Panel.SPELLS);
                    }).bounds(x + OSLConfig.SPELLS_BUTTON_X.get(), y + 5 + OSLConfig.SPELLS_BUTTON_Y.get(), 16, 16).build());

            // Combat Style Buttons (Only when Stats panel is active)
            if (activePanel == Panel.STATS) {
                Minecraft mc = Minecraft.getInstance();
                int styleX = inv.getLeftPos() + 180 + 5;
                int styleY = inv.getTopPos() + 25;
                for (CombatStyle style : CombatStyle.values()) {
                    final CombatStyle s = style;
                    event.addListener(Button.builder(Component.empty(), (_) -> {
                                assert mc.player != null;
                                mc.player.setData(ModAttachments.COMBAT_STYLE.get(), s);
                        ClientPacketDistributor.sendToServer(new ChangeStylePayload(s));
                        inv.init(inv.width, inv.height); // Refresh to update highlights
                    }).bounds(styleX, styleY, 32, 11)
                      .build());
                    styleX += 33;
                }
            }
        }
    }

    private static void togglePanel(Panel panel) {
        activePanel = (activePanel == panel) ? Panel.NONE : panel;
        // Reset scroll position whenever switching panels
        SkillUnlocksScreen.resetScroll();
        // Re-init screen to add/remove the panel-specific buttons immediately
        if (Minecraft.getInstance().screen instanceof InventoryScreen inv) {
            inv.init(inv.width, inv.height);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof InventoryScreen && activePanel != Panel.NONE) {
            // Route scroll events to our panels
            if (activePanel == Panel.UNLOCKS) {
                SkillUnlocksScreen.handleScroll(event.getScrollDeltaY());
            }
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;
        if (event.getButton() != 0) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();

        // Handle Button Grabbing
        if (isShiftDown()) {
            int x = getBaseX(inv) + 77;
            int y = inv.getTopPos();

            if (isMouseOver(mx, my, x + OSLConfig.STATS_BUTTON_X.get(), y + 43 + OSLConfig.STATS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.STATS_BUTTON_X.get(), OSLConfig.STATS_BUTTON_Y.get(), DraggedButton.STATS);
            } else if (isMouseOver(mx, my, x + OSLConfig.SKILLS_BUTTON_X.get(), y + 25 + OSLConfig.SKILLS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.SKILLS_BUTTON_X.get(), OSLConfig.SKILLS_BUTTON_Y.get(), DraggedButton.SKILLS);
            } else if (isMouseOver(mx, my, x + OSLConfig.SPELLS_BUTTON_X.get(), y + 7 + OSLConfig.SPELLS_BUTTON_Y.get())) {
                grab(mx, my, OSLConfig.SPELLS_BUTTON_X.get(), OSLConfig.SPELLS_BUTTON_Y.get(), DraggedButton.SPELLS);
            }
        } 
        
        // Handle Panel Interactions (Clicking skills to open unlocks, etc.)
        if (activePanel != Panel.NONE) {
            int panelX = inv.getLeftPos() + 180; // Docked relative to the moving inventory
            int panelY = inv.getTopPos();
            
            if (activePanel == Panel.SKILLS) {
                LevelScreen.handleOverlayClick(mx, my, panelX, panelY);
            } else if (activePanel == Panel.STATS) {
                CharacterStatsScreen.handleOverlayClick(mx, my, panelX, panelY);
            } else if (activePanel == Panel.SPELLS) {
                SpellScreen.handleOverlayClick(mx, my, panelX, panelY);
            }
            
            // Handle "X" Close click for panels
            int closeXOffset = switch (activePanel) {
                case STATS -> 145;
                case SPELLS -> 85;
                case SKILLS, UNLOCKS -> 105;
                default -> 100;
            };

            if (isMouseOver(mx, my, panelX + closeXOffset - 15, panelY + 2)) {
                // Play UI Click Sound
                Minecraft mc = Minecraft.getInstance();
                assert mc.level != null;
                assert mc.player != null;
                mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
                
                // If closing Unlocks, go back to Skills. Otherwise, close the panel.
                if (activePanel == Panel.UNLOCKS) activePanel = Panel.SKILLS;
                else activePanel = Panel.NONE;
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
    public static void onForegroundRender(ContainerScreenEvent.Render.Foreground event) {
        if (event.getContainerScreen() instanceof InventoryScreen inv) {
            int recipeBookShift = inv.getLeftPos() - getBaseX(inv);

            int x = 77 - recipeBookShift;
            int y = 0;

            // Render STATS icon
            event.getGuiGraphics().item(new ItemStack(Items.NETHERITE_HELMET),
                    x + OSLConfig.STATS_BUTTON_X.get(), y + 41 + OSLConfig.STATS_BUTTON_Y.get());

            // Render SKILLS icon
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(
                    (x + OSLConfig.SKILLS_BUTTON_X.get() + 1.5f),
                    (y + 23 + OSLConfig.SKILLS_BUTTON_Y.get() + 1.5f),
                    event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.8f, 0.8f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.EXPERIENCE_BOTTLE), 0, 0);
            event.getGuiGraphics().pose().popMatrix();

            // Render SPELLS icon
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(
                    (float)(x + OSLConfig.SPELLS_BUTTON_X.get() + 2),
                    (float)(y + 5 + OSLConfig.SPELLS_BUTTON_Y.get() + 2),
                    event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.75f, 0.75f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.ENCHANTED_BOOK), 0, 0);
            event.getGuiGraphics().pose().popMatrix();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPostRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            int px = inv.getLeftPos() + 180;
            int py = inv.getTopPos();

            if (activePanel != Panel.NONE) {
                // Render the Active Panel Overlay
                // Because this is 'Post', it renders above JEI and vanilla UI elements
                switch (activePanel) {
                    case STATS -> CharacterStatsScreen.renderOverlay(event.getGuiGraphics(), px, py, event.getMouseX(), event.getMouseY());
                    case SKILLS -> LevelScreen.renderOverlay(event.getGuiGraphics(), px, py, event.getMouseX(), event.getMouseY());
                    case SPELLS -> SpellScreen.renderOverlay(event.getGuiGraphics(), px, py, event.getMouseX(), event.getMouseY());
                    case UNLOCKS -> SkillUnlocksScreen.renderOverlay(event.getGuiGraphics(), px, py, selectedSkill);
                }
            }

            // Render Button Tooltips Manually (After sidebar to ensure they are on top)
            int bx = getBaseX(inv) + 77;
            int by = inv.getTopPos();
            Minecraft mc = Minecraft.getInstance();
            String tooltipText = null;

            if (isMouseOver(event.getMouseX(), event.getMouseY(), bx + OSLConfig.STATS_BUTTON_X.get(), by + 41 + OSLConfig.STATS_BUTTON_Y.get())) {
                tooltipText = "Character Sheet (Shift+Drag to Move)";
            } else if (isMouseOver(event.getMouseX(), event.getMouseY(), bx + OSLConfig.SKILLS_BUTTON_X.get(), by + 23 + OSLConfig.SKILLS_BUTTON_Y.get())) {
                tooltipText = "Skills (Shift+Drag to Move)";
            } else if (isMouseOver(event.getMouseX(), event.getMouseY(), bx + OSLConfig.SPELLS_BUTTON_X.get(), by + 5 + OSLConfig.SPELLS_BUTTON_Y.get())) {
                tooltipText = "Spellbook (Shift+Drag to Move)";
            }

            if (tooltipText != null) {
                event.getGuiGraphics().tooltip(mc.font, List.of(ClientTooltipComponent.create(Component.literal(tooltipText).getVisualOrderText())), 
                        event.getMouseX(), event.getMouseY(), DefaultTooltipPositioner.INSTANCE, null);
            }
        }
    }

    // 176 is the standard width of the inventory texture. 
    // This calculates where the inventory SHOULD be if the recipe book was closed.
    private static int getBaseX(InventoryScreen inv) {
        return (inv.width - 176) / 2;
    }

    private static boolean isShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }
}
