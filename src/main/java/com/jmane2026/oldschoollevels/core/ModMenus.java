package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.menus.SigilPouchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(Registries.MENU, OldSchoolLevels.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SigilPouchMenu>> SIGIL_POUCH_MENU = 
            MENUS.register("sigil_pouch", () -> IMenuTypeExtension.create(SigilPouchMenu::new));
}