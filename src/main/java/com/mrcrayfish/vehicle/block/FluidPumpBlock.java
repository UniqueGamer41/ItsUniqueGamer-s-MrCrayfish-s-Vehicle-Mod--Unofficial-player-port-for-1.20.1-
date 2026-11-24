package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.block.entity.PipeBlockEntity;
import com.mrcrayfish.vehicle.block.entity.PumpBlockEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mrcrayfish.vehicle.block.RotatedEntityObjectBlock.createTickerHelper;

/**
 * Author: MrCrayfish
 */
public class FluidPumpBlock extends FluidPipeBlock implements EntityBlock
{
    public static final DirectionProperty DIRECTION = BlockStateProperties.FACING;

    public static final VoxelShape[] PUMP_BOX = new VoxelShape[]{
            box(3, 0, 3, 13, 4, 13),
            box(3, 12, 3, 13, 16, 13),
            box(3, 3, 0, 13, 13, 4),
            box(3, 3, 12, 13, 13, 16),
            box(0, 3, 3, 4, 13, 13),
            box(12, 3, 3, 16, 13, 13)
    };

    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext p_60558_)
    {
        return this.getPumpShape(state, worldIn, pos);
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return this.getPumpShape(state, level, pos);
    }

    protected VoxelShape getPumpShape(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(super.getPipeShape(state, worldIn, pos));
        shapes.add(PUMP_BOX[this.getCollisionFacing(state).get3DDataValue()]);
        return VoxelShapeHelper.combineAll(shapes);
    }

    protected Direction getCollisionFacing(BlockState state)
    {
        return state.getValue(DIRECTION).getOpposite();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if(super.use(state, level, pos, player, hand, result) == InteractionResult.SUCCESS)
        {
            return InteractionResult.SUCCESS;
        }

        if(!level.isClientSide())
        {
            PipeBlockEntity tileEntity = getPipeTileEntity(level, pos);
            if(tileEntity instanceof PumpBlockEntity pumpTileEntity)
            {

                /*if(!FMLLoader.isProduction())
                {
                    pumpTileEntity.invalidatePipeNetwork();
                }*/

                Vec3 localHitVec = result.getLocation().add(-pos.getX(), -pos.getY(), -pos.getZ());
                if(player.getItemInHand(hand).getItem() == ModItems.WRENCH.get() && this.isLookingAtHousing(state, localHitVec))
                {
                    pumpTileEntity.cyclePowerMode();
                    this.invalidatePipeNetwork(level, pos);
                    Vec3 vec = result.getLocation();
                    level.playSound(null, vec.x(), vec.y(), vec.z(), SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + 0.1F * level.random.nextFloat());
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public boolean isLookingAtHousing(BlockState state, Vec3 hitVec)
    {
        VoxelShape shape = PUMP_BOX[this.getCollisionFacing(state).get3DDataValue()];
        AABB boundingBox = shape.bounds();
        return boundingBox.inflate(0.001).contains(hitVec);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState replaceState, boolean what)
    {
        if(!state.is(replaceState.getBlock()))
        {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof PumpBlockEntity)
            {
                ((PumpBlockEntity) tileEntity).removePumpFromPipes();
            }
        }
        super.onRemove(state, world, pos, replaceState, what);
    }

    @Override
    protected void invalidatePipeNetwork(Level world, BlockPos pos)
    {
        super.invalidatePipeNetwork(world, pos);

        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof PumpBlockEntity)
        {
            ((PumpBlockEntity) tileEntity).invalidatePipeNetwork();
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        BlockState state = this.defaultBlockState().setValue(DIRECTION, face);
        state = this.getPipeState(state, world, pos);
        state = this.getDisabledState(state, world, pos);
        return state;
    }

    @Override
    public BlockState getDisabledState(BlockState state, Level world, BlockPos pos)
    {
        boolean disabled = false;
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof PumpBlockEntity pump)
        {
            disabled = !pump.getPowerMode().test(pump);
        }
        state = state.setValue(DISABLED, disabled);
        return state;
    }

    @Override
    protected boolean canPipeConnectTo(BlockState state, LevelAccessor world, BlockPos pos, Direction direction)
    {
        if(direction == state.getValue(DIRECTION).getOpposite())
            return false;
        return super.canPipeConnectTo(state, world, pos, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION);
    }

    @Nullable
    @Override
    public PumpBlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new PumpBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        if(!level.isClientSide())
        {
            return createTickerHelper(type, ModTileEntities.FLUID_PUMP.get(), PumpBlockEntity::onServerTick);
        }

        return null;
    }
}
