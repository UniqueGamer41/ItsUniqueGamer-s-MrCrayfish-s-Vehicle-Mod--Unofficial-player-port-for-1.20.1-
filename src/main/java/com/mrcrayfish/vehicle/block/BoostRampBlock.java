package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.util.Bounds;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.block.entity.BoostBlockEntity;
import com.mrcrayfish.vehicle.util.StateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class BoostRampBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    //TODO redo collisions
    private static final VoxelShape BASE = Shapes.box(0, 0, 0, 1, 0.0625, 1);
    private static final VoxelShape STACKED_BASE = Shapes.box(0, 0, 0, 1, 0.5625, 1);
    private static final VoxelShape SELECTION_BOTTOM = Shapes.box(0, 0, 0, 1, 0.5, 1);
    private static final VoxelShape[] COLLISION = buildCollision(false);
    private static final VoxelShape[] COLLISION_STACKED = buildCollision(true);

    private static VoxelShape[] buildCollision(boolean stacked)
    {
        VoxelShape[] result = new VoxelShape[4];
        for(int facing = 0; facing < 4; facing++)
        {
            VoxelShape shape = stacked ? STACKED_BASE : BASE;
            for(int i = 1; i <= 7; i++)
            {
                int y = stacked ? 8 + i : i;
                shape = Shapes.or(shape, new Bounds(2 * i, y, 0, 16, y + 1, 16).getRotatedBounds()[facing]);
            }
            result[facing] = shape;
        }
        return result;
    }

    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return state.getValue(STACKED) ? Shapes.block() : SELECTION_BOTTOM;
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        int index = state.getValue(DIRECTION).get2DDataValue();
        return state.getValue(STACKED) ? COLLISION_STACKED[index] : COLLISION[index];
    }

    public BoostRampBlock()
    {
        super(Properties.of()
                .mapColor(MapColor.STONE) // replaces Material.STONE
                .strength(1.0F));

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(STACKED, false)
                .setValue(LEFT, false)
                .setValue(RIGHT, false));
    }


    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity)
    {
        if(entity instanceof PoweredVehicleEntity && entity.getControllingPassenger() != null)
        {
            Direction facing = state.getValue(DIRECTION);
            if(facing == entity.getDirection())
            {
                float speedMultiplier = 0.0F;
                BlockEntity tileEntity = level.getBlockEntity(pos);
                if(tileEntity instanceof BoostBlockEntity)
                {
                    speedMultiplier = ((BoostBlockEntity) tileEntity).getSpeedMultiplier();
                }

                PoweredVehicleEntity poweredVehicle = (PoweredVehicleEntity) entity;
                if(!poweredVehicle.isBoosting())
                {
                    level.playSound(null, pos, ModSounds.BLOCK_BOOST_PAD_BOOST.get(), SoundSource.BLOCKS, 2.0F, 0.5F);
                }
                poweredVehicle.setBoosting(true);
                poweredVehicle.setLaunching(2);
                poweredVehicle.setSpeedMultiplier(speedMultiplier);
                if(poweredVehicle instanceof LandVehicleEntity landVehicle)
                {
                    landVehicle.applyBoostKick();
                }
                double launchSpeed = Math.max(poweredVehicle.getSpeed(), poweredVehicle.getMaxSpeed());
                double strength = Config.SERVER.boostRampLaunchStrength.get();
                Vec3 motion = poweredVehicle.getDeltaMovement();
                poweredVehicle.setDeltaMovement(new Vec3(motion.x, (launchSpeed * strength) / 20F + 0.1, motion.z));
            }
        }
    }


    @Override
    @NotNull
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighbourState, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockPos neighbourPos)
    {
        return this.getRampState(state, world, pos, state.getValue(DIRECTION));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx)
    {
        BlockState state = super.getStateForPlacement(ctx);
        if(state == null) return null;
        return this.getRampState(state, ctx.getLevel(), ctx.getClickedPos(), state.getValue(DIRECTION));
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
    {
        ItemStack held = player.getItemInHand(hand);
        if(held.is(ModItems.BOOST_RAMP.get()) && hit.getDirection() == Direction.UP && !state.getValue(STACKED))
        {
            if(!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(STACKED, true), 3);
                level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
                if(!player.getAbilities().instabuild)
                {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private BlockState getRampState(BlockState state, LevelAccessor world, BlockPos pos, Direction facing)
    {
        state = state.setValue(LEFT, false);
        state = state.setValue(RIGHT, false);
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.LEFT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.LEFT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(RIGHT, true);
            }
        }
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(LEFT, true);
            }
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(STACKED);
        builder.add(LEFT);
        builder.add(RIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new BoostBlockEntity(pos, state, Config.SERVER.boostRampMultiplier.get().floatValue());
    }
}
