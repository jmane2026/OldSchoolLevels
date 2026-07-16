package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
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

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class InventoryStyleOverlay {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            // Character Sheet Button
            Button statsButton = Button.builder(Component.empty(), (_) -> mc.setScreen(new CharacterStatsScreen()))
                    .bounds(inv.getLeftPos() + 77, inv.getTopPos() + 43, 16, 16)
                    .tooltip(Tooltip.create(Component.literal("Character Sheet")))
                    .build();

            // Skills Window Button (Experience Bottle icon)
            Button skillsButton = Button.builder(Component.empty(), (_) -> mc.setScreen(new LevelScreen(Component.literal("Skills"))))
                    .bounds(inv.getLeftPos() + 77, inv.getTopPos() + 25, 16, 16)
                    .tooltip(Tooltip.create(Component.literal("View Skills")))
                    .build();

            // Spellbook Button - Positioned ABOVE the Skills button
            Button spellbookButton = Button.builder(Component.empty(), _ -> Minecraft.getInstance().setScreen(new SpellScreen(Component.literal("Spells"))))
                    .bounds(inv.getLeftPos() + 77, inv.getTopPos() + 7, 16, 16)
                    .tooltip(Tooltip.create(Component.literal("Spellbook")))
                    .build();
            event.addListener(spellbookButton);

            event.addListener(statsButton);
            event.addListener(skillsButton);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            // Draw the Netherite Helmet icon directly over the button location
            event.getGuiGraphics().item(new ItemStack(Items.NETHERITE_HELMET), inv.getLeftPos() + 77, inv.getTopPos() + 43);

            // Draw a book or experience bottle for skills
            event.getGuiGraphics().pose().pushMatrix();
            // Translate to center, scale, then render at 0,0 relative to the translated matrix
            event.getGuiGraphics().pose().translate(inv.getLeftPos() + 77 + 1.5f, inv.getTopPos() + 25 + 1.5f, event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.8f, 0.8f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.EXPERIENCE_BOTTLE), 0, 0);
            event.getGuiGraphics().pose().popMatrix();

            // Draw the Enchanted Book icon scaled down to 12x12
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(inv.getLeftPos() + 79, inv.getTopPos() + 9, event.getGuiGraphics().pose());
            event.getGuiGraphics().pose().scale(0.75f, 0.75f, event.getGuiGraphics().pose());
            event.getGuiGraphics().item(new ItemStack(Items.ENCHANTED_BOOK), 0, 0);
            event.getGuiGraphics().pose().popMatrix();
        }
    }
}