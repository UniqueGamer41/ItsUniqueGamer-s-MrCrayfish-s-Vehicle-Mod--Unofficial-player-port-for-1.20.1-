package com.mrcrayfish.vehicle.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class FuelSlot extends Slot
{
    public FuelSlot(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return AbstractFurnaceBlockEntity.isFuel(stack) || isBucket(stack);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack)
    {
        return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
    }

    public static boolean isBucket(ItemStack stack)
    {
        return stack.getItem() == Items.BUCKET;
    }
}
