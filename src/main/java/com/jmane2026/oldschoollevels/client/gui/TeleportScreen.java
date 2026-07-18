package com.jmane2026.oldschoollevels.client.gui;

import com.jmane2026.oldschoollevels.common.TeleportLocation;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.network.TeleportActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TeleportScreen extends Screen {
    private final boolean isPortal;
    private EditBox nameInput;
    private boolean isAdding = false;
    private TeleportLocation selectedLocation = null;
    private static final int WIDTH = 150;
    private static final int HEIGHT = 180;

    public TeleportScreen(boolean isPortal) {
        super(Component.literal(isPortal ? "Create Portal" : "Teleport"));
        this.isPortal = isPortal;
    }

    @Override
    protected void init() {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        // --- Controls Bar ---
        int btnY = y + HEIGHT - 25;

        // Plus Button (Toggle Add Mode)
        this.addRenderableWidget(Button.builder(Component.literal("+"), (_) -> {
            this.isAdding = !this.isAdding;
            playClickSound();
            this.rebuildWidgets();
        }).bounds(x + 10, btnY, 20, 20).build());

        // Minus Button (Remove Selected)
        Button deleteBtn = Button.builder(Component.literal("-"), (_) -> {
            if (selectedLocation != null) {
                ClientPacketDistributor.sendToServer(new TeleportActionPayload("", selectedLocation, TeleportActionPayload.Action.DELETE, isPortal));
                this.selectedLocation = null;
                playClickSound();
                this.rebuildWidgets();
            }
        }).bounds(x + 35, btnY, 20, 20).build();
        deleteBtn.active = selectedLocation != null;
        this.addRenderableWidget(deleteBtn);

        // Execute Button (Teleport/Portal)
        Button goBtn = Button.builder(Component.literal(isPortal ? "Open" : "Go"), (_) -> {
            if (selectedLocation != null) {
                ClientPacketDistributor.sendToServer(new TeleportActionPayload("", selectedLocation, TeleportActionPayload.Action.EXECUTE, isPortal));
                this.onClose();
            }
        }).bounds(x + 60, btnY, 80, 20).build();
        goBtn.active = selectedLocation != null;
        this.addRenderableWidget(goBtn);

        // --- Add Mode Overlay ---
        if (isAdding) {
            this.nameInput = new EditBox(this.font, x + 10, y + 60, 130, 20, Component.literal("Name..."));
            this.addRenderableWidget(nameInput);
            this.setInitialFocus(this.nameInput);

            this.addRenderableWidget(Button.builder(Component.literal("Save"), (_) -> {
                if (!nameInput.getValue().isEmpty()) {
                    ClientPacketDistributor.sendToServer(new TeleportActionPayload(nameInput.getValue(), null, TeleportActionPayload.Action.ADD, isPortal));
                    this.isAdding = false;
                    playClickSound();
                    this.rebuildWidgets();
                }
            }).bounds(x + 10, y + 85, 130, 20).build());
        }

        // Small "X" button in top right
        this.addRenderableWidget(Button.builder(Component.literal("X"), (_) -> this.onClose())
                .bounds(x + WIDTH - 16, y + 2, 14, 14).build());
    }

    private void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        double mouseX = event.x();
        double mouseY = event.y();

        // Check buttons first to prevent selection logic from "stealing" the click
        if (super.mouseClicked(event, doubleClicked)) return true;

        if (event.button() == 0 && !isAdding) {
            int x = (this.width - WIDTH) / 2;
            int y = (this.height - HEIGHT) / 2;
            int listX = x + 10;
            int listY = y + 25;

            // Check if clicking within the list bounds
            if (mouseX >= listX && mouseX <= listX + 130) {
                assert Minecraft.getInstance().player != null;
                List<TeleportLocation> locations = Minecraft.getInstance().player.getData(ModAttachments.TELEPORT_LOCATIONS.get());
                int clickedIdx = (int) ((mouseY - listY) / 22);

                if (clickedIdx >= 0 && clickedIdx < locations.size()) {
                    TeleportLocation clicked = locations.get(clickedIdx);

                    // Toggle selection
                    if (clicked.equals(selectedLocation)) {
                        selectedLocation = null;
                    } else {
                        selectedLocation = clicked;
                    }

                    playClickSound();
                    this.rebuildWidgets(); // Rebuild to update Go/Delete button states
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        // 1. Fill the main inner panel background with vanilla gray
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);

        // 2. Draw the white highlight lines (Top and Left borders)
        graphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF); // Top edge
        graphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF); // Left edge

        // 3. Draw the dark gray shadow lines (Bottom and Right borders)
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF555555); // Bottom edge
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF555555); // Right edge

        // Title - Converted to drawString with NO drop shadow and dark gray font
        int titleWidth = this.font.width(this.title);
        graphics.text(this.font, this.title, x + (WIDTH / 2) - (titleWidth / 2), y + 8, 0xFF404040, false);

        // --- Render List Manually (Not as buttons) ---
        if (!isAdding) {
            assert Minecraft.getInstance().player != null;
            List<TeleportLocation> locations = Minecraft.getInstance().player.getData(ModAttachments.TELEPORT_LOCATIONS.get());
            int listY = y + 25;
            for (TeleportLocation loc : locations) {
                int textColor = 0xFF404040; // Default text color changed to crisp dark gray

                if (loc.equals(selectedLocation)) {
                    textColor = 0xFF0000AA; // Active selection color changed to indigo blue for clean visibility
                    // Selection indicator character - Drawn with shadow set to false
                    graphics.text(this.font, ">", x + 4, listY, 0xFF0000AA, false);
                }

                // Render location name with shadow set to false
                graphics.text(this.font, loc.name(), x + 12, listY, textColor, false);
                listY += 22;
                if (listY > y + HEIGHT - 45) break;
            }
        }

        // If adding, draw a clean recessed overlay area behind the input text boxes instead of pitch black
        if (isAdding) {
            graphics.fill(x + 5, y + 55, x + WIDTH - 5, y + 110, 0xFF8B8B8B); // Dark slot gray
            graphics.outline(x + 5, y + 55, WIDTH - 10, 55, 0xFF373737); // Dark inset outline border
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}