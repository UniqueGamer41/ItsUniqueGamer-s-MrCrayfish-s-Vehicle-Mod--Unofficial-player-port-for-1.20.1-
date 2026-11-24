package com.mrcrayfish.vehicle.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class LazyBlockItem<T extends Block> extends BlockItem
{

    public LazyBlockItem(Supplier<T> block, Properties properties)
    {
        super(block.get(), properties);
    }
}
