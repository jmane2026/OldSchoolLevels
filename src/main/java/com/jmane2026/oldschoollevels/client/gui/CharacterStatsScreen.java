package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.common.RequirementUtils;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.ChangeStylePayload;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CharacterStatsScreen extends Screen {
    private static final int WIDTH = 145; // Widened further for full labels
    private static final int HEIGHT = 166;

    public static void renderOverlay(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        IAttachmentHolder holder = mc.player;
        if (holder == null) return;

        SkillData data = holder.getData(ModAttachments.SKILLS.get());
        CombatStyle currentStyle = holder.getData(ModAttachments.COMBAT_STYLE.get());

        // Background
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);
        graphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF555555);
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF555555);

        int titleWidth = mc.font.width("Stats");
        graphics.text(mc.font, "Stats", x + (WIDTH / 2) - (titleWidth / 2), y + 6, 0xFF404040, false);

        // Style Selection Header
        graphics.text(mc.font, "Combat Style:", x + 5, y + 16, 0xFF404040, false);

        int styleX = x + 5;
        int styleY = y + 25;
        CombatStyle hoveredStyle = null;

        for (CombatStyle style : CombatStyle.values()) {
            boolean active = style == currentStyle;
            // Draw Button Visual
            Identifier sprite = active ? Identifier.withDefaultNamespace("widget/button_highlighted") : Identifier.withDefaultNamespace("widget/button");
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, styleX, styleY, 32, 11);
            
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)styleX + 2, (float)styleY + 3, graphics.pose());
            graphics.pose().scale(0.42f, 0.42f, graphics.pose()); // Shrink text to fit buttons
            graphics.text(mc.font, style.getName().getString(), 0, 0, 0xFF404040, false);
            graphics.pose().popMatrix();

            // Check for hover to render manual tooltip (since vanilla tooltips render behind Post)
            if (mouseX >= styleX && mouseX < styleX + 32 && mouseY >= styleY && mouseY < styleY + 11) {
                hoveredStyle = style;
            }

            styleX += 33;
        }

        int statY = y + 42;
        int lineGap = 8;
        float scale = 0.7f;

        renderStatOverlay(graphics, mc, "Strength:", getStrBonusStatic(data, currentStyle), x + 5, statY, scale);
        renderStatOverlay(graphics, mc, "Attack Speed:", getAtkBonusStatic(data, currentStyle), x + 5, statY += lineGap, scale);
        renderStatOverlay(graphics, mc, "Defense:", getDefBonusStatic(data, currentStyle), x + 5, statY += lineGap, scale);
        renderStatOverlay(graphics, mc, "Health:", getLifeBonusStatic(data), x + 5, statY += lineGap, scale);
        renderStatOverlay(graphics, mc, "Ranged Damage:", getRangedBonusStatic(data), x + 5, statY += lineGap, scale);
        renderStatOverlay(graphics, mc, "Magic Power:", getMagicBonusStatic(data), x + 5, statY += lineGap, scale);
        
        int mobLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MOBILITY));
        renderStatOverlay(graphics, mc, "Movement Speed:", String.format("+%.1f%%", (mobLvl - 1) * 1.5f), x + 5, statY += lineGap + 4, scale);
        renderStatOverlay(graphics, mc, "Swim Speed:", String.format("+%d%%", (int)(RequirementUtils.getSwimSpeedBonus(mobLvl) * 100)), x + 5, statY += lineGap, scale);
        renderStatOverlay(graphics, mc, "Max Stamina:", String.format("%.0f", RequirementUtils.getMaxStamina(mobLvl)), x + 5, statY += lineGap, scale);

        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int woodLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));
        renderStatOverlay(graphics, mc, "Mining Speed:", "+" + (miningLvl - 1) + "%", x + 5, statY += lineGap + 4, scale);
        renderStatOverlay(graphics, mc, "Woodcutting Speed:", "+" + (woodLvl - 1) + "%", x + 5, statY + lineGap, scale);

        // Render the manual tooltip last so it is on top of the sidebar
        if (hoveredStyle != null) {
            // Split the tooltip string by newlines and convert each line into a tooltip component
            List<ClientTooltipComponent> tooltipComponents = new java.util.ArrayList<>();
            for (String line : getStyleTooltip(hoveredStyle).getString().split("\n")) {
                tooltipComponents.add(ClientTooltipComponent.create(Component.literal(line).getVisualOrderText()));
            }

            graphics.tooltip(mc.font, tooltipComponents, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }
    }

    public static Component getStyleTooltip(CombatStyle style) {
        return switch (style) {
            case ACCURATE -> Component.literal("§eAccurate§r\nFocuses on precision.\nProvides +100% Attack Speed bonus scaling.");
            case AGGRESSIVE -> Component.literal("§eAggressive§r\nFocuses on power.\nProvides +100% Strength bonus scaling.");
            case DEFENSIVE -> Component.literal("§eDefensive§r\nFocuses on protection.\nProvides +100% Defense bonus scaling.");
            case CONTROLLED -> Component.literal("§eControlled§r\nBalanced approach.\nSplits bonus between Attack, Strength, and Defense.");
        };
    }

    public static void handleOverlayClick(double ignoredMx, double ignoredMy, int ignoredX, int ignoredY) {
        // Click logic moved to Button widgets in InventoryStyleOverlay
    }

    private static void renderStatOverlay(GuiGraphicsExtractor graphics, Minecraft mc, String label, String value, int x, int y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)x, (float)y, graphics.pose());
        graphics.pose().scale(scale, scale, graphics.pose());
        graphics.text(mc.font, label, 0, 0, 0xFF404040, false);
        graphics.text(mc.font, value, 120, 0, 0xFF0000AA, false); // Adjusted for 140px width
        graphics.pose().popMatrix();
    }

    public CharacterStatsScreen() {
        super(Component.literal("Character Sheet"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - (WIDTH / 2);
        int startY = centerY - (HEIGHT / 2);

        IAttachmentHolder holder = Minecraft.getInstance().player;
        if (holder == null) return;

        // Add Style Selection Buttons
        CombatStyle[] styles = CombatStyle.values();
        for (int i = 0; i < styles.length; i++) {
            CombatStyle style = styles[i];
            this.addRenderableWidget(Button.builder(style.getName(), (_) -> {
                holder.setData(ModAttachments.COMBAT_STYLE.get(), style);
                ClientPacketDistributor.sendToServer(new ChangeStylePayload(style));
            }).bounds(startX + 10, startY + 30 + (i * 22), 160, 20).build());
        }

        // Small "X" button in the top right corner
        this.addRenderableWidget(Button.builder(Component.literal("X"), (_) -> this.onClose())
                .bounds(startX + WIDTH - 18, startY + 2, 14, 14)
                .build());
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
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        IAttachmentHolder holder = Minecraft.getInstance().player;
        if (holder == null) return;

        SkillData data = holder.getData(ModAttachments.SKILLS.get());
        CombatStyle currentStyle = holder.getData(ModAttachments.COMBAT_STYLE.get());

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - (WIDTH / 2);
        int startY = centerY - (HEIGHT / 2);

        // 1. Fill the main inner panel background with vanilla gray
        graphics.fill(startX, startY, startX + WIDTH, startY + HEIGHT, 0xFFC6C6C6);

        // 2. Draw the white highlight lines (Top and Left borders)
        graphics.fill(startX, startY, startX + WIDTH, startY + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(startX, startY, startX + 1, startY + HEIGHT, 0xFFFFFFFF); // Left edge

        // 3. Draw the dark gray shadow lines (Bottom and Right borders)
        graphics.fill(startX, startY + HEIGHT - 1, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(startX + WIDTH - 1, startY, startX + WIDTH, startY + HEIGHT, 0xFF555555); // Right edge

        // 4. DRAW SELECTION HIGHLIGHT HERE (On top of the gray panel, before text)
        int activeIdx = currentStyle.ordinal();
        int highlightY = startY + 30 + (activeIdx * 22);
        graphics.outline(startX + 10 - 1, highlightY - 1, 162, 22, 0xFFFFFF00);

        // Title - Converted to drawString with NO drop shadow and dark gray font
        int titleWidth = this.font.width(this.title);
        graphics.text(this.font, this.title, centerX - (titleWidth / 2), startY + 8, 0xFF404040, false);

        // Combat Section Header - Changed from 0xFFBBBBBB to crisp dark gray 0xFF404040
        int combatY = startY + 120;
        graphics.text(this.font, "Combat Bonuses:", startX + 10, combatY, 0xFF404040, false);

        int statY = combatY + 11;
        int lineGap = 8; // Tight gap

        renderStat(graphics, "Strength:", getStrBonusStatic(data, currentStyle), startX + 15, statY);
        renderStat(graphics, "Swing Speed:", getAtkBonusStatic(data, currentStyle), startX + 15, statY += lineGap);
        renderStat(graphics, "Defense:", getDefBonusStatic(data, currentStyle), startX + 15, statY += lineGap);
        renderStat(graphics, "Health:", getLifeBonusStatic(data), startX + 15, statY += lineGap);
        renderStat(graphics, "Ranged Dmg:", getRangedBonusStatic(data), startX + 15, statY += lineGap);
        renderStat(graphics, "Magic Bonus:", getMagicBonusStatic(data), startX + 15, statY += lineGap);

        int mobilityY = statY + 11;
        int mobLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MOBILITY));
        renderStat(graphics, "Move Speed:", String.format("+%.1f%%", (mobLvl - 1) * 1.5f), startX + 15, mobilityY);
        renderStat(graphics, "Swim:", String.format("+%d%%", (int)(RequirementUtils.getSwimSpeedBonus(mobLvl) * 100)), startX + 15, mobilityY + lineGap);
        renderStat(graphics, "Stamina:", String.format("%.0f", RequirementUtils.getMaxStamina(mobLvl)), startX + 15, mobilityY + (lineGap * 2));

        // Gathering Section Header - Changed from 0xFFBBBBBB to crisp dark gray 0xFF404040
        int gatherY = mobilityY + (lineGap * 3) + 1;
        graphics.text(this.font, "Gathering Bonuses:", startX + 10, gatherY, 0xFF404040, false);

        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
        int woodLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));

        renderStat(graphics, "Mining:", "+" + (miningLvl - 1) + "%", startX + 15, gatherY + 12);
        renderStat(graphics, "Wood Cutting:", "+" + (woodLvl - 1) + "%", startX + 15, gatherY + 22);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    private void renderStat(GuiGraphicsExtractor graphics, String label, String value, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float)x, (float)y, graphics.pose());
        graphics.pose().scale(0.85f, 0.85f, graphics.pose());

        // 1. Draw label at origin with NO drop shadow - Changed to dark charcoal for contrast
        graphics.text(this.font, label, 0, 0, 0xFF404040, false);

        // 2. Draw value at 100 with NO drop shadow - Changed to dark blue (vanilla stat style)
        graphics.text(this.font, value, 100, 0, 0xFF0000AA, false);

        graphics.pose().popMatrix();
    }


    public static String getStrBonusStatic(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.STRENGTH));
        double bonus = ((lvl - 1) * style.getStrengthScale());
        return String.format("+%.1f%%", bonus);
    }

    public static String getAtkBonusStatic(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.ATTACK));
        double bonus = (lvl - 1) * 0.05 * style.getAttackSpeedScale();
        return String.format("+%.2f", bonus);
    }

    public static String getDefBonusStatic(SkillData data, CombatStyle style) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.DEFENSE));
        double armor = RequirementUtils.getDefenseArmorBonus(lvl) * style.getDefenseScale();
        double max = RequirementUtils.getDefenseArmorBonus(lvl);
        return String.format("%.1f / %.1f", armor, max);
    }

    public static String getLifeBonusStatic(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.LIFE));
        return String.format("+%.1f HP", Math.max(0, (lvl - 10) * 0.5));
    }

    public static String getRangedBonusStatic(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.RANGED));
        return String.format("+%d%%", lvl - 1);
    }

    public static String getMagicBonusStatic(SkillData data) {
        int lvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));
        // 1% per level logic: Level 6 = +6%
        return String.format("+%d%%", lvl - 1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}