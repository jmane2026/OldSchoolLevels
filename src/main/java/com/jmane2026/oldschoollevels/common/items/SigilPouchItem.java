package com.jmane2026.oldschoollevels.common.items;

import com.jmane2026.oldschoollevels.client.gui.SigilPouchScreen;
import com.jmane2026.oldschoollevels.common.menus.SigilPouchMenu;
import com.jmane2026.oldschoollevels.core.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SigilPouchItem extends Item {
    public SigilPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new SigilPouchMenu(id, inv, stack), Component.literal("Sigil Pouch")));
        }
        return InteractionResult.SUCCESS;
    }

    public static int getSigilCount(ItemStack pouch, Item sigil) {
        Map<String, Integer> contents = pouch.getOrDefault(ModDataComponents.SIGIL_STORAGE.get(), new HashMap<>());
        return contents.getOrDefault(BuiltInRegistries.ITEM.getKey(sigil).toString(), 0);
    }

    public static void addSigils(ItemStack pouch, Item sigil, int amount) {
        Map<String, Integer> contents = new HashMap<>(pouch.getOrDefault(ModDataComponents.SIGIL_STORAGE.get(), new HashMap<>()));
        String key = BuiltInRegistries.ITEM.getKey(sigil).toString();
        contents.put(key, contents.getOrDefault(key, 0) + amount);
        pouch.set(ModDataComponents.SIGIL_STORAGE.get(), contents);
    }

    public static boolean consumeSigils(ItemStack pouch, Item sigil, int amount) {
        Map<String, Integer> contents = new HashMap<>(pouch.getOrDefault(ModDataComponents.SIGIL_STORAGE.get(), new HashMap<>()));
        String key = BuiltInRegistries.ITEM.getKey(sigil).toString();
        int current = contents.getOrDefault(key, 0);
        if (current >= amount) {
            contents.put(key, current - amount);
            pouch.set(ModDataComponents.SIGIL_STORAGE.get(), contents);
            return true;
        }
        return false;
    }
    
    public static void removeSigils(ItemStack pouch, Item sigil, int amount) {
        Map<String, Integer> contents = new HashMap<>(pouch.getOrDefault(ModDataComponents.SIGIL_STORAGE.get(), new HashMap<>()));
        String key = BuiltInRegistries.ITEM.getKey(sigil).toString();
        int current = contents.getOrDefault(key, 0);
        int toRemove = Math.min(current, amount);
        if (toRemove > 0) {
            contents.put(key, current - toRemove);
            pouch.set(ModDataComponents.SIGIL_STORAGE.get(), contents);
        }
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
}