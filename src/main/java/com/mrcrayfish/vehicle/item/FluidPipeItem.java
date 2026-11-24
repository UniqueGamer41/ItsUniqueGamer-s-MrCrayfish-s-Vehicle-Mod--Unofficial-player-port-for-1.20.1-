package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.VehicleMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class FluidPipeItem extends BlockItem
{
    public FluidPipeItem(Supplier<Block> block)
    {
        super(block.get(), new Item.Properties());
    }
}
