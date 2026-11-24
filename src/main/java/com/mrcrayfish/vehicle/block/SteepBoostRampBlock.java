package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.block.entity.BoostBlockEntity;
import com.mrcrayfish.vehicle.util.Bounds;
import com.mrcrayfish.vehicle.util.StateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SteepBoostRampBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    private static final VoxelShape COLLISION_BASE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);

    private static final VoxelShape[] COLLISION = buildCollision();

    private static VoxelShape[] buildCollision()
    {
        VoxelShape[] result = new VoxelShape[4];
        for(int facing = 0; facing < 4; facing++)
        {
            VoxelShape shape = COLLISION_BASE;
            for(int i = 1; i <= 15; i++)
            {
                shape = Shapes.or(shape, new Bounds(i, i, 0, 16, i + 1, 16).getRotatedBounds()[facing]);
            }
            result[facing] = shape;
        }
        return result;
    }

    @Override
    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return COLLISION[state.getValue(DIRECTION).get2DDataValue()];
    }

    public SteepBoostRampBlock()
    {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
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
                poweredVehicle.setLaunching(3);
                poweredVehicle.setSpeedMultiplier(speedMultiplier);
                if(poweredVehicle instanceof LandVehicleEntity landVehicle)
                {
                    landVehicle.applyBoostKick();
                }
                double launchSpeed = Math.max(poweredVehicle.getSpeed(), poweredVehicle.getMaxSpeed());
                double strength = Config.SERVER.steepBoostRampLaunchStrength.get();
                Vec3 motion = poweredVehicle.getDeltaMovement();
                poweredVehicle.setDeltaMovement(new Vec3(motion.x, (launchSpeed * strength) / 20F + 0.1, motion.z));
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT);
        builder.add(RIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BoostBlockEntity(pos, state, Config.SERVER.steepBoostRampMultiplier.get().floatValue());
    }
}
