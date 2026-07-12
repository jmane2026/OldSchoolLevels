package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.blocks.entity.MagicPortalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OldSchoolLevels.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicPortalBlockEntity>> MAGIC_PORTAL_BE = BLOCK_ENTITIES.register(
            "magic_portal_be",
            () -> new BlockEntityType<>(
                    MagicPortalBlockEntity::new,
                    Set.of(ModBlocks.MAGIC_PORTAL.get())
            )
    );
}