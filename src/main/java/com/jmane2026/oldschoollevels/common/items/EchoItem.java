package com.jmane2026.oldschoollevels.common.items;

import com.jmane2026.oldschoollevels.core.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EchoItem extends Item {
    private final String structureId;

    public EchoItem(Properties properties, String structureId) {
        super(properties);
        this.structureId = structureId;
    }

    @Override
    public void inventoryTick(@NonNull ItemStack itemStack, @NonNull ServerLevel level, @NonNull Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof ServerPlayer player) {
            // Check if the item is in the main hand or offhand
            boolean isSelected = (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND);

            if (isSelected) {
                // Only search for structures every 100 ticks (5 seconds) for performance
                // Added: Only search if the player doesn't already have a target locked
                BlockPos currentTarget = player.getData(ModAttachments.ECHO_TARGET.get());

                if (currentTarget.equals(BlockPos.ZERO) && player.tickCount % 100 == 0) {
                    TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE,
                            Identifier.fromNamespaceAndPath("oldschoollevels", structureId + "s"));

                    BlockPos nearest = level.findNearestMapStructure(tag, player.blockPosition(), 1000, false);

                    if (nearest != null) {
                        setTargetCenter(player, level, tag, nearest);
                        // Manually sync the attachment so the Client HUD can see the new target
                        player.syncData(ModAttachments.ECHO_TARGET.get());
                    }
                }
            }
        }
    }

    public void updateAndSync(ServerPlayer player, ServerLevel level) {
        // Only perform the instant sync if they don't already have a target
        // This prevents the target from jumping the moment they pull the item out
        // if the server already had a valid position stored.
        BlockPos currentTarget = player.getData(ModAttachments.ECHO_TARGET.get());
        if (currentTarget.equals(BlockPos.ZERO)) {
            TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath("oldschoollevels", structureId + "s"));

            BlockPos nearest = level.findNearestMapStructure(tag, player.blockPosition(), 1000, false);
            setTargetCenter(player, level, tag, nearest);
            player.syncData(ModAttachments.ECHO_TARGET.get());
        }
    }

    private void setTargetCenter(ServerPlayer player, ServerLevel level, TagKey<Structure> tag, BlockPos nearest) {
        if (nearest != null) {
            // getStructureWithPieceAt returns the structure start (which contains the bounding box) for the given BlockPos
            var start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), tag);
            if (start.isValid()) {
                player.setData(ModAttachments.ECHO_TARGET.get(), start.getBoundingBox().getCenter());
                return;
            }
            player.setData(ModAttachments.ECHO_TARGET.get(), nearest);
        } else {
            player.setData(ModAttachments.ECHO_TARGET.get(), BlockPos.ZERO);
        }
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(@NonNull ItemInstance instance) {
        // We must convert to ItemStack to manipulate damage, then return as Instance
        if (instance instanceof ItemStack stack) {
            // Return the Echo item itself as an ItemStackTemplate, making it non-consumable
            return ItemStackTemplate.fromNonEmptyStack(stack.copy());
        }
        return null;
    }
    
    @Override
    public boolean isFoil(@NonNull ItemStack stack) { return true; } // Glow like a compass/talisman
}