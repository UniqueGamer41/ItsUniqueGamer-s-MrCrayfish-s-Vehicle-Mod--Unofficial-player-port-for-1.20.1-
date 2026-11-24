package com.mrcrayfish.vehicle.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public interface IStorageBlock extends Container, MenuProvider
{
    NonNullList<ItemStack> getInventory();

    @Override
    default int getContainerSize()
    {
        return this.getInventory().size();
    }

    @Override
    default boolean isEmpty()
    {
        for(ItemStack itemstack : this.getInventory())
        {
            if(!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    @NotNull
    default ItemStack getItem(int index)
    {
        return index >= 0 && index < this.getInventory().size() ? this.getInventory().get(index) : ItemStack.EMPTY;
    }

    @Override
    @NotNull
    default ItemStack removeItem(int index, int count)
    {
        ItemStack stack = ContainerHelper.removeItem(this.getInventory(), index, count);
        if (!stack.isEmpty())
        {
            this.setChanged();
        }

        return stack;
    }

    @Override
    @NotNull
    default ItemStack removeItemNoUpdate(int index)
    {
        ItemStack stack = this.getInventory().get(index);
        if (!stack.isEmpty())
        {
            this.getInventory().set(index, ItemStack.EMPTY);
            return stack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    default void setItem(int index, @NotNull ItemStack stack)
    {
        this.getInventory().set(index, stack);
        if(!stack.isEmpty() && stack.getCount() > this.getMaxStackSize())
        {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    default boolean stillValid(@NotNull Player player)
    {
        return false;
    }

    @Override
    default void clearContent()
    {
        this.getInventory().clear();
    }
}
