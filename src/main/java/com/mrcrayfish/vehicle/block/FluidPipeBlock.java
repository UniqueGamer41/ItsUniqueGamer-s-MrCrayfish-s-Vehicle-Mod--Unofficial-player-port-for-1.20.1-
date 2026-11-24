package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.item.WrenchItem;
import com.mrcrayfish.vehicle.block.entity.PipeBlockEntity;
import com.mrcrayfish.vehicle.block.entity.PumpBlockEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import it.unimi.dsi.fastutil.longs.LongSet;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
public class FluidPipeBlock extends ObjectBlock implements EntityBlock
{
    public static final BooleanProperty[] CONNECTED_PIPES = {BlockStateProperties.DOWN, BlockStateProperties.UP, BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.EAST};
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");

    protected static final VoxelShape CENTER = Block.box(5, 5, 5, 11, 11, 11);
    protected static final VoxelShape[] SIDES = {
            Block.box(5, 0, 5, 11, 5, 11), Block.box(5, 11, 5, 11, 16, 11),
            Block.box(5, 5, 0, 11, 11, 5), Block.box(5, 5, 11, 11, 11, 16),
            Block.box(0, 5, 5, 5, 11, 11), Block.box(11, 5, 5, 16, 11, 11),
            CENTER
    };

    public FluidPipeBlock()
    {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.05F)
                .sound(SoundType.NETHERITE_BLOCK));

        BlockState defaultState = this.getStateDefinition().any().setValue(DISABLED, true);
        for(BooleanProperty property : CONNECTED_PIPES)
        {
            defaultState = defaultState.setValue(property, false);
        }

        this.registerDefaultState(defaultState);
    }

    @Nullable
    public static PipeBlockEntity getPipeTileEntity(BlockGetter world, BlockPos pos)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof PipeBlockEntity ? (PipeBlockEntity) tileEntity : null;
    }

    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return this.getPipeShape(state, level, pos);
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return this.getPipeShape(state, level, pos);
    }

    public VoxelShape getPipeShape(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        List<VoxelShape> shapes = new ArrayList<>();
        boolean[] disabledConnections = this.getDisabledConnections(worldIn, pos);
        for(int i = 0; i < Direction.values().length; i++)
        {
            if(state.getValue(CONNECTED_PIPES[i]) && !disabledConnections[i])
            {
                shapes.add(SIDES[i]);
            }
        }
        shapes.addAll(Arrays.asList(SIDES).subList(Direction.values().length, SIDES.length));
        return VoxelShapeHelper.combineAll(shapes);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        PipeBlockEntity pipe = getPipeTileEntity(level, pos);
        Pair<AABB, Direction> hit = this.getConnectionBox(level, pos, state, player, hand, result.getDirection(), result.getLocation(), pipe);
        if(pipe != null && hit != null)
        {
            Direction direction = hit.getRight();
            boolean enabled = !pipe.isConnectionDisabled(direction);
            pipe.setConnectionState(direction, enabled);
            BlockState newState = state.setValue(CONNECTED_PIPES[direction.get3DDataValue()], !enabled);
            level.setBlockAndUpdate(pos, newState);
            level.sendBlockUpdated(pos, state, newState, 3 & 8);
            this.invalidatePipeNetwork(level, pos);

            // Also changes the state of the adjacent connection
            BlockPos relativePos = pos.relative(direction);
            PipeBlockEntity adjacentPipe = getPipeTileEntity(level, relativePos);
            if(adjacentPipe != null)
            {
                Direction opposite = direction.getOpposite();
                adjacentPipe.setConnectionState(opposite, enabled);
                BlockState relativeState = adjacentPipe.getBlockState();
                BlockState newRelativeState = relativeState.setValue(CONNECTED_PIPES[opposite.get3DDataValue()], !enabled);
                level.setBlockAndUpdate(relativePos, newRelativeState);
                level.sendBlockUpdated(relativePos, relativeState, newRelativeState, 3 & 8);
                FluidPipeBlock relativeBlock = (FluidPipeBlock) relativeState.getBlock();
                relativeBlock.invalidatePipeNetwork(level, relativePos);
            }

            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.IRON_GOLEM_STEP, SoundSource.BLOCKS, 1.0F, 2.0F);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    protected Pair<AABB, Direction> getConnectionBox(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, Vec3 hitVec, @Nullable PipeBlockEntity pipe)
    {
        Vec3 localHitVec = hitVec.add(-pos.getX(), -pos.getY(), -pos.getZ());
        if(pipe == null || !(player.getItemInHand(hand).getItem() instanceof WrenchItem))
        {
            return null;
        }
        for(int i = 0; i < Direction.values().length + 1; i++)
        {
            boolean isCenter = i == Direction.values().length;
            if((isCenter || state.getValue(CONNECTED_PIPES[i])) && SIDES[i].bounds().inflate(0.001).contains(localHitVec))
            {
                if(!isCenter)
                {
                    facing = Direction.from3DDataValue(i);
                }
                else if(!state.getValue(CONNECTED_PIPES[facing.get3DDataValue()]))
                {
                    BlockPos adjacentPos = pos.relative(facing);
                    BlockState adjacentState = world.getBlockState(adjacentPos);
                    Block adjacentBlock = adjacentState.getBlock();

                    if(adjacentBlock != ModBlocks.FLUID_PIPE.get() && adjacentBlock != ModBlocks.FLUID_PUMP.get())
                    {
                        BlockEntity adjacentTileEntity = world.getBlockEntity(adjacentPos);
                        if(adjacentTileEntity == null || !adjacentTileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite()).isPresent())
                        {
                            return null;
                        }
                    }
                }

                if(world.getBlockState(pos.relative(facing)).getBlock() != Blocks.LEVER)
                {
                    return new ImmutablePair<>(SIDES[i].bounds().move(pos), facing);
                }
            }
        }
        return null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag)
    {
        if(state.getBlock() == newState.getBlock())
            return;

        PipeBlockEntity tileEntity = this.newBlockEntity(pos, state);
        if(tileEntity != null)
        {
            for(Direction direction : Direction.values())
            {
                BlockEntity relativeTileEntity = level.getBlockEntity(pos.relative(direction));
                if(relativeTileEntity instanceof PipeBlockEntity)
                {
                    tileEntity.getDisabledConnections()[direction.get3DDataValue()] = ((PipeBlockEntity) relativeTileEntity).isConnectionDisabled(direction.getOpposite());
                }
            }
            level.setBlockEntity(tileEntity);
            FluidNetworkHandler.instance().addPipeForUpdate(tileEntity);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean p_60514_)
    {
        boolean disabled = this.getDisabledState(state, level, pos).getValue(DISABLED);
        if(state.getValue(DISABLED) != disabled)
        {
            this.invalidatePipeNetwork(level, pos);
            if(state.getBlock() instanceof FluidPumpBlock)
            {
                level.setBlock(pos, state.setValue(DISABLED, disabled), (1 << 1) | (1 << 3));
            }
        }

        BlockState newState = this.getPipeState(state, level, pos);
        for(Direction direction : Direction.values())
        {
            int index = direction.get3DDataValue();
            if(newState.getValue(CONNECTED_PIPES[index]) != state.getValue(CONNECTED_PIPES[index]))
            {
                this.invalidatePipeNetwork(level, pos);
                break;
            }
        }
    }

    protected BlockState getDisabledState(BlockState state, Level world, BlockPos pos)
    {
        boolean disabled = world.hasNeighborSignal(pos);
        state = state.setValue(DISABLED, disabled);
        return state;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState replaceState, boolean what)
    {
        if(!state.is(replaceState.getBlock()))
        {
            this.invalidatePipeNetwork(world, pos);
            super.onRemove(state, world, pos, replaceState, what);
        }
    }

    protected void invalidatePipeNetwork(Level world, BlockPos pos)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof PipeBlockEntity)
        {
            LongSet pumps = ((PipeBlockEntity) tileEntity).getPumps();

            for(long pump : pumps)
            {
                BlockPos pumpPos = BlockPos.of(pump);
                BlockEntity blockEntity = world.getBlockEntity(pumpPos);
                if(blockEntity instanceof PumpBlockEntity pumpBlockEntity)
                {
                    pumpBlockEntity.invalidatePipeNetwork();
                }
            }
        }
    }

    @Override
    @NotNull
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighbourState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighbourPos)
    {
        return this.getPipeState(state, level, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockPos pos = ctx.getClickedPos();
        BlockState state = this.defaultBlockState();
        state = this.getPipeState(state, ctx.getLevel(), pos);
        state = this.getDisabledState(state, ctx.getLevel(), pos);
        state = this.getPlacedDisabledState(state, ctx.getLevel(), pos);
        return state;
    }

    protected BlockState getPlacedDisabledState(BlockState state, Level world, BlockPos pos)
    {
        if(!state.getValue(DISABLED))
        {
            state = state.setValue(DISABLED, true);
            for(Direction direction : Direction.values())
            {
                BlockPos relativePos = pos.relative(direction);
                BlockEntity relativeTileEntity = world.getBlockEntity(relativePos);
                if(relativeTileEntity instanceof PipeBlockEntity pipeTileEntity)
                {
                    if(!pipeTileEntity.getDisabledConnections()[direction.getOpposite().get3DDataValue()])
                    {
                        BlockState relativeState = pipeTileEntity.getBlockState();
                        if(!relativeState.getValue(DISABLED))
                        {
                            if(relativeState.getBlock() instanceof FluidPumpBlock)
                            {
                                if(relativeState.getValue(FluidPumpBlock.DIRECTION) == direction)
                                {
                                    continue;
                                }
                            }

                            state = state.setValue(DISABLED, false);
                            break;
                        }
                    }
                }
            }
        }
        return state;
    }

    protected BlockState getPipeState(BlockState state, LevelAccessor world, BlockPos pos)
    {
        boolean[] disabledConnections = this.getDisabledConnections(world, pos);
        for(Direction direction : Direction.values())
        {
            state = state.setValue(CONNECTED_PIPES[direction.get3DDataValue()], false);

            if(disabledConnections[direction.get3DDataValue()] && world.getBlockState(pos.relative(direction)).getBlock() != Blocks.LEVER)
                continue;

            state = state.setValue(CONNECTED_PIPES[direction.get3DDataValue()], this.canPipeConnectTo(state, world, pos, direction));
        }
        return state;
    }

    protected boolean canPipeConnectTo(BlockState state, LevelAccessor world, BlockPos pos, Direction direction)
    {
        BlockPos relativePos = pos.relative(direction);
        BlockEntity adjacentTileEntity = world.getBlockEntity(relativePos);
        if(adjacentTileEntity instanceof PipeBlockEntity)
        {
            BlockState relativeState = world.getBlockState(relativePos);
            if(relativeState.getBlock() instanceof FluidPumpBlock)
            {
                if(relativeState.getValue(FluidPumpBlock.DIRECTION) == direction)
                {
                    return false;
                }
            }
            return !((PipeBlockEntity) adjacentTileEntity).isConnectionDisabled(direction.getOpposite());
        }
        else if(adjacentTileEntity != null && adjacentTileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent())
        {
            return true;
        }
        else
        {
            BlockState adjacentState = world.getBlockState(relativePos);
            if(adjacentState.getBlock() == Blocks.LEVER)
            {
                AttachFace attachFace = adjacentState.getValue(LeverBlock.FACE);
                if(direction.getAxis() != Direction.Axis.Y)
                {
                    return adjacentState.getValue(LeverBlock.FACING) == direction && attachFace == AttachFace.WALL;
                }
                else if(direction == Direction.UP && attachFace == AttachFace.FLOOR)
                {
                    return true;
                }
                return direction == Direction.DOWN && attachFace == AttachFace.CEILING;
            }
            return false;
        }
    }

    @Override
    @NotNull
    public VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos)
    {
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTED_PIPES);
        builder.add(DISABLED);
    }

    @Nullable
    @Override
    public PipeBlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new PipeBlockEntity(pos, state);
    }

    protected boolean[] getDisabledConnections(BlockGetter reader, BlockPos pos)
    {
        PipeBlockEntity tileEntity = getPipeTileEntity(reader, pos);
        return tileEntity != null ? tileEntity.getDisabledConnections() : new boolean[Direction.values().length];
    }
}
