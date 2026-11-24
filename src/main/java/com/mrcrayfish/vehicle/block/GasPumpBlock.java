package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.block.entity.GasPumpTankBlockEntity;
import com.mrcrayfish.vehicle.block.entity.GasPumpBlockEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
public class GasPumpBlock extends RotatedEntityObjectBlock
{
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();

    public GasPumpBlock()
    {
        super(Properties.of()
                .strength(1.0F)
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(TOP, false));
    }

    private VoxelShape getShape(BlockState state)
    {
        if (SHAPES.containsKey(state))
        {
            return SHAPES.get(state);
        }
        Direction direction = state.getValue(DIRECTION);
        boolean top = state.getValue(TOP);
        List<VoxelShape> shapes = new ArrayList<>();
        if (top)
        {
            shapes.add(VoxelShapeHelper.getHorizontalShapes(VoxelShapeHelper.rotate(box(3, -16, 0, 13, 15, 16), Direction.EAST))[direction.get2DDataValue()]);
        }
        else
        {
            shapes.add(VoxelShapeHelper.getHorizontalShapes(VoxelShapeHelper.rotate(box(3, 0, 0, 13, 31, 16), Direction.EAST))[direction.get2DDataValue()]);
        }
        VoxelShape shape = VoxelShapeHelper.combineAll(shapes);
        SHAPES.put(state, shape);
        return shape;
    }

    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return this.getShape(state);
    }

    @Override
    @NotNull
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result)
    {
        if(level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        if(state.getValue(TOP))
        {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof GasPumpBlockEntity gasPump)
            {
                if(gasPump.getFuelingEntity() != null && gasPump.getFuelingEntity().getId() == player.getId())
                {
                    gasPump.setFuelingEntity(null);
                    level.playSound(null, pos, ModSounds.BLOCK_GAS_PUMP_NOZZLE_PUT_DOWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                else if(state.getValue(DIRECTION).getClockWise().equals(result.getDirection()))
                {
                    gasPump.setFuelingEntity(player);
                    level.playSound(null, pos, ModSounds.BLOCK_GAS_PUMP_NOZZLE_PICK_UP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }
        else if(FluidUtil.interactWithFluidHandler(player, hand, level, pos, result.getDirection()))
        {
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, LevelReader level, @NotNull BlockPos pos)
    {
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack)
    {
        level.setBlockAndUpdate(pos.above(), state.setValue(TOP, true));
    }

    @Override
    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        if (!level.isClientSide())
        {
            boolean top = state.getValue(TOP);
            BlockPos blockpos = pos.relative(top ? Direction.DOWN : Direction.UP);
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.getBlock() == state.getBlock() && blockstate.getValue(TOP) != top)
            {
                level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @NotNull
    public List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder)
    {
        if (state.getValue(TOP))
        {
            Vec3 origin = builder.getOptionalParameter(LootContextParams.ORIGIN);
            if (origin != null)
            {
                BlockPos pos = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z);
                BlockEntity tileEntity = builder.getLevel().getBlockEntity(pos.below());
                if (tileEntity != null)
                {
                    builder = builder.withParameter(LootContextParams.BLOCK_ENTITY, tileEntity);
                }
            }
        }
        return super.getDrops(state, builder);
    }



    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(TOP);
    }

    @Override
    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, BlockState state)
    {
        if (state.getValue(TOP))
        {
            return new GasPumpBlockEntity(pos, state);
        }
        return new GasPumpTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, BlockState state, @NotNull BlockEntityType<T> type)
    {
        if(state.getValue(TOP))
        {
            if(level.isClientSide())
            {
                return createTickerHelper(type, ModTileEntities.GAS_PUMP.get(), GasPumpBlockEntity::onClientTick);
            }
            return createTickerHelper(type, ModTileEntities.GAS_PUMP.get(), GasPumpBlockEntity::onServerTick);
        }

        return null;
    }
}