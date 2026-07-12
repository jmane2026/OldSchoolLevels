package com.jmane2026.oldschoollevels.common.blocks.entity;

import com.jmane2026.oldschoollevels.common.TeleportLocation;
import com.jmane2026.oldschoollevels.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MagicPortalBlockEntity extends BlockEntity {
    private TeleportLocation destination;
    private int ticksRemaining = 2400; // 2 Minutes (20 ticks * 60 seconds * 2)

    public MagicPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_PORTAL_BE.get(), pos, state);
    }

    public void setDestination(TeleportLocation loc) {
        this.destination = loc;
        setChanged();
    }

    public TeleportLocation getDestination() {
        return destination;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicPortalBlockEntity be) {
        if (level.isClientSide()) return;

        be.ticksRemaining--;
        if (be.ticksRemaining <= 0) {
            // Removing the master (bottom) block triggers the updateShape in the top block,
            // clearing the whole portal.
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ticks_remaining", ticksRemaining);
        if (destination != null) {
            output.store("destination", TeleportLocation.CODEC, destination);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ticksRemaining = input.getInt("ticks_remaining").orElse(2400);
        input.read("destination", TeleportLocation.CODEC).ifPresent(loc -> this.destination = loc);
    }
}