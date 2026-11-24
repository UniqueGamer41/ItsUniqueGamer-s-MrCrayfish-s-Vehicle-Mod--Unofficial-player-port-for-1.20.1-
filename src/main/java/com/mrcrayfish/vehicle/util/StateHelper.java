package com.mrcrayfish.vehicle.util;

import com.mrcrayfish.vehicle.block.RotatedObjectBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public class StateHelper
{
    public static Block getBlock(LevelAccessor world, BlockPos pos, Direction facing, RelativeDirection dir)
    {
        BlockPos target = getBlockPosRelativeTo(world, pos, facing, dir);
        return world.getBlockState(target).getBlock();
    }

    public static RelativeDirection getRotation(LevelAccessor world, BlockPos pos, Direction facing, RelativeDirection dir)
    {
        BlockPos target = getBlockPosRelativeTo(world, pos, facing, dir);
        Direction other = world.getBlockState(target).getValue(RotatedObjectBlock.DIRECTION);
        return getDirectionRelativeTo(facing, other);
    }

    public static boolean isAirBlock(LevelAccessor world, BlockPos pos, Direction facing, RelativeDirection dir)
    {
        BlockPos target = getBlockPosRelativeTo(world, pos, facing, dir);
        return world.getBlockState(target).isAir();
    }

    private static BlockPos getBlockPosRelativeTo(LevelAccessor world, BlockPos pos, Direction facing, RelativeDirection dir)
    {
        return switch (dir) {
            case LEFT -> pos.relative(facing.getClockWise());
            case RIGHT -> pos.relative(facing.getCounterClockWise());
            case UP -> pos.relative(facing);
            case DOWN -> pos.relative(facing.getOpposite());
            default -> pos;
        };
    }

    private static RelativeDirection getDirectionRelativeTo(Direction thisBlock, Direction otherBlock)
    {
        int num = thisBlock.get2DDataValue() - otherBlock.get2DDataValue();
        return switch (num) {
            case -3, 1 -> RelativeDirection.LEFT;
            case -2, 2 -> RelativeDirection.UP;
            case -1, 3 -> RelativeDirection.RIGHT;
            case 0 -> RelativeDirection.DOWN;
            default -> RelativeDirection.NONE;
        };
    }

    public enum RelativeDirection
    {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE
    }
}
