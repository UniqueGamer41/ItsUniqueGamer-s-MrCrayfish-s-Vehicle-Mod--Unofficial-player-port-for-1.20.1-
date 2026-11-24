package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankBlockEntity extends BlockEntityFluidHandlerSynced
{
    public GasPumpTankBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.GAS_PUMP_TANK.get(), Config.SERVER.gasPumpCapacity.get(), pos, state);
    }
}