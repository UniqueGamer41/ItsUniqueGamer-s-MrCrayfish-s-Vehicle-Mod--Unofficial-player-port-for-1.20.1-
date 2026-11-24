package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumBlockEntity extends FuelDrumBlockEntity
{
    public IndustrialFuelDrumBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.INDUSTRIAL_FUEL_DRUM.get(), ModBlocks.INDUSTRIAL_FUEL_DRUM.get().getCapacity(), pos, state);
    }
}
