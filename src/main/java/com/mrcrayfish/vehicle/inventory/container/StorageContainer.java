package com.mrcrayfish.vehicle.inventory.container;

import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.common.slot.SlotStorage;
import com.mrcrayfish.vehicle.init.ModContainers;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class StorageContainer extends AbstractContainerMenu
{
    private final StorageInventory storageInventory;
    private final int numRows;

    public StorageContainer(int windowId, Inventory playerInventory, StorageInventory storageInventory, Player player)
    {
        super(ModContainers.STORAGE.get(), windowId);
        this.storageInventory = storageInventory;
        this.numRows = storageInventory.getContainerSize() / 9;
        storageInventory.startOpen(player);
        int yOffset = (this.numRows - 4) * 18;

        for(int y = 0; y < this.numRows; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new SlotStorage(storageInventory, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 103 + y * 18 + yOffset));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 161 + yOffset));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn)
    {
        return this.storageInventory.stillValid(playerIn);
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if(index < this.numRows * 9)
            {
                if(!this.moveItemStackTo(itemstack1, this.numRows * 9, this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.moveItemStackTo(itemstack1, 0, this.numRows * 9, false))
            {
                return ItemStack.EMPTY;
            }

            if(itemstack1.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(@NotNull Player playerIn)
    {
        super.removed(playerIn);
        this.storageInventory.stopOpen(playerIn);
    }

    public Container getStorageInventory()
    {
        return this.storageInventory;
    }
}
