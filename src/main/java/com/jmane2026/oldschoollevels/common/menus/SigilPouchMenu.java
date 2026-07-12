package com.jmane2026.oldschoollevels.common.menus;

import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import com.jmane2026.oldschoollevels.core.ModItems;
import com.jmane2026.oldschoollevels.core.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SigilPouchMenu extends AbstractContainerMenu {
    private final ItemStack pouchStack;
    private static final List<Item> SIGIL_TYPES = List.of(
            ModItems.AIR_SIGIL.get(),
            ModItems.WATER_SIGIL.get(), ModItems.EARTH_SIGIL.get(),
            ModItems.FIRE_SIGIL.get(), ModItems.LOGIC_SIGIL.get()
    );

    // Client constructor
    public SigilPouchMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, playerInv.player.getMainHandItem().getItem() instanceof SigilPouchItem
                ? playerInv.player.getMainHandItem()
                : playerInv.player.getOffhandItem());
    }

    // Server constructor
    public SigilPouchMenu(int containerId, Inventory playerInv, ItemStack pouch) {
        super(ModMenus.SIGIL_POUCH_MENU.get(), containerId);
        this.pouchStack = pouch;

        // Create a single container with enough size for all pouch slots
        SimpleContainer pouchContainer = new SimpleContainer(SIGIL_TYPES.size());

        // Pouch Slots (0-5) - Aligned to match the Screen rendering logic
        for (int i = 0; i < SIGIL_TYPES.size(); i++) {
            // Y starts at 84 to match the first row of the inventory grid
            this.addSlot(new Slot(pouchContainer, i, -87 + (i % 3 * 26), 84 + (i / 3 * 26)) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        // Handle interaction with Virtual Pouch Slots
        if (slotId >= 0 && slotId < SIGIL_TYPES.size()) {
            Item sigil = SIGIL_TYPES.get(slotId);
            ItemStack carried = getCarried();

            if (carried.isEmpty()) {
                // Withdraw 1 stack (64)
                int count = SigilPouchItem.getSigilCount(pouchStack, sigil);
                if (count > 0) {
                    int toTake = Math.min(count, 64);
                    SigilPouchItem.removeSigils(pouchStack, sigil, toTake);
                    setCarried(new ItemStack(sigil, toTake));
                }
            } else if (carried.getItem() == sigil) {
                // Deposit held stack
                SigilPouchItem.addSigils(pouchStack, sigil, carried.getCount());
                setCarried(ItemStack.EMPTY);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

            if (path.contains("_sigil") && !path.contains("blank")) {
                // Shift-click directly into the virtual storage
                SigilPouchItem.addSigils(pouchStack, stack.getItem(), stack.getCount());
                slot.set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            // Standard inventory movement
            if (index >= SIGIL_TYPES.size() && index < SIGIL_TYPES.size() + 27) {
                if (!this.moveItemStackTo(stack, SIGIL_TYPES.size() + 27, SIGIL_TYPES.size() + 36, false)) return ItemStack.EMPTY;
            } else if (index >= SIGIL_TYPES.size() + 27 && index < SIGIL_TYPES.size() + 36) {
                if (!this.moveItemStackTo(stack, SIGIL_TYPES.size(), SIGIL_TYPES.size() + 27, false)) return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == pouchStack || player.getOffhandItem() == pouchStack;
    }

    public List<Item> getSigilTypes() { return SIGIL_TYPES; }
    public ItemStack getPouch() { return pouchStack; }
}