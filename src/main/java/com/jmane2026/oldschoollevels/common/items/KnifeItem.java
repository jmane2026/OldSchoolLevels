package com.jmane2026.oldschoollevels.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class KnifeItem extends Item {
    public KnifeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
        itemStack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            itemStack.hurtAndBreak(1, owner, owner.getUsedItemHand());
        }
        return true;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        // We must convert to ItemStack to manipulate damage, then return as Instance
        if (instance instanceof ItemStack stack) {
            ItemStack remainder = stack.copy();
            // Apply 1 durability damage
            remainder.setDamageValue(stack.getDamageValue() + 1);

            // If the tool is broken, it disappears
            if (remainder.getDamageValue() >= remainder.getMaxDamage()) {
                return null;
            }
            // Return the damaged copy as a template to stay in the crafting grid
            return ItemStackTemplate.fromNonEmptyStack(remainder);
        }
        return null;
    }
}