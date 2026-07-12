package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class UnlockToast implements Toast {
    private final Skill skill;
    private final String description;
    private final ItemStack icon;
    private Visibility visibility = Visibility.SHOW;

    public UnlockToast(Skill skill, String description, ItemStack icon) {
        this.skill = skill;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public void update(ToastManager toastManager, long timeSinceLastVisible) {
        this.visibility = timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeSinceLastVisible) {
        // Draw background box
        graphics.fill(0, 0, this.width(), 32, 0xCC111115);
        graphics.outline(0, 0, this.width(), 32, 0xFFFFFFFF);

        graphics.text(font, Component.literal(skill.getDisplayName() + " Unlock!"), 32, 7, 0xFFFFFF00);
        graphics.text(font, Component.literal(description), 32, 18, 0xFFFFFFFF);

        // Logic to render actual Spell PNGs if this is a Magic unlock
        boolean renderedCustom = false;
        if (this.skill == Skill.MAGIC) {
            for (Spell spell : Spell.values()) {
                if (description.equals(spell.getDisplayName())) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, spell.getIconTexture(), 
                            8, 8, 0.0f, 0.0f, 16, 16, 32, 32, 32, 32, -1);
                    renderedCustom = true;
                    break;
                }
            }
        }

        if (!renderedCustom) {
            graphics.fakeItem(icon, 8, 8);
        }
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public int width() {
        return 160;
    }

    public static void add(ToastManager component, Skill skill, String description, ItemStack icon) {
        component.addToast(new UnlockToast(skill, description, icon));
    }

    @Override
    public Object getToken() { return skill; }
}