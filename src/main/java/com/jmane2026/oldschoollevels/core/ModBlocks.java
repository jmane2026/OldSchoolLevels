package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.blocks.MagicPortalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OldSchoolLevels.MODID);

    public static final DeferredBlock<Block> SIGILIC_ORE = BLOCKS.registerBlock("sigilic_ore",
            Block::new,
            p ->p.requiresCorrectToolForDrops()
                    .strength(2.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final DeferredBlock<MagicPortalBlock> MAGIC_PORTAL = BLOCKS.registerBlock("magic_portal",
            MagicPortalBlock::new,
            _ -> BlockBehaviour.Properties.of()
                    .noCollision()
                    .noOcclusion()
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .lightLevel(_ -> 15));
}