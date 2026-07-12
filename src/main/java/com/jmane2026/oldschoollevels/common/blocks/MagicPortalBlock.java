package com.jmane2026.oldschoollevels.common.blocks;

import com.jmane2026.oldschoollevels.common.blocks.entity.MagicPortalBlockEntity;
import com.jmane2026.oldschoollevels.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class MagicPortalBlock extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 7.0D, 14.0D, 16.0D, 9.0D);

    public MagicPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (directionToNeighbour.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (directionToNeighbour == Direction.UP)) {
            if (!neighbourState.is(this) || neighbourState.getValue(HALF) == half) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos masterPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            if (level.getBlockEntity(masterPos) instanceof MagicPortalBlockEntity portal) {
                var loc = portal.getDestination();
                if (loc != null) {
                    player.teleportTo(level.getServer().getLevel(loc.dimension()), loc.pos().getX() + 0.5, loc.pos().getY(), loc.pos().getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new MagicPortalBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only the bottom half needs to tick to manage the timer
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;

        return type == ModBlockEntities.MAGIC_PORTAL_BE.get() 
                ? (lvl, pos, st, be) -> MagicPortalBlockEntity.tick(lvl, pos, st, (MagicPortalBlockEntity) be) 
                : null;
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}