package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.block.entity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
public class FluidExtractorBlock extends RotatedEntityObjectBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public FluidExtractorBlock()
    {
        super(Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.METAL) // replaces Material.HEAVY_METAL
                .strength(1.0F)
                .noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(ENABLED, false));
    }


    @Override
    @NotNull
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result)
    {
        if(!level.isClientSide)
        {
            ItemStack stack = player.getItemInHand(hand);
            if(stack.is(Items.BUCKET))
            {
                FluidUtil.interactWithFluidHandler(player, hand, level, pos, result.getDirection());
                return InteractionResult.SUCCESS;
            }

            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof FluidExtractorBlockEntity)
            {
                TileEntityUtil.sendUpdatePacket(tileEntity, (ServerPlayer) player);
                NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) tileEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(state.getBlock() != newState.getBlock())
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof Container)
            {
                Containers.dropContents(level, pos, (Container) blockEntity);
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);

        builder.add(ENABLED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new FluidExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        if(!level.isClientSide)
        {
            return createTickerHelper(type, ModTileEntities.FLUID_EXTRACTOR.get(), FluidExtractorBlockEntity::onServerTick);
        }

        return null;
    }
}