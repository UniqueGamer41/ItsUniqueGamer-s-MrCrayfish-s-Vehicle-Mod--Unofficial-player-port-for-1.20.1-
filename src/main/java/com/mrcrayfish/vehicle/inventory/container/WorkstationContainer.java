package com.mrcrayfish.vehicle.inventory.container;

import com.mrcrayfish.vehicle.init.ModContainers;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.WheelItem;
import com.mrcrayfish.vehicle.block.entity.WorkstationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class WorkstationContainer extends AbstractContainerMenu
{
    private final WorkstationBlockEntity workstationTileEntity;
    private final BlockPos pos;

    public WorkstationContainer(int windowId, Inventory playerInventory, WorkstationBlockEntity workstationTileEntity)
    {
        super(ModContainers.WORKSTATION.get(), windowId);
        System.out.println("[DEBUG] Server-side WorkstationContainer created");
        this.workstationTileEntity = workstationTileEntity;
        this.pos = workstationTileEntity.getBlockPos();

        this.addSlot(new Slot(workstationTileEntity, 0, 173, 30)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack)
            {
                return stack.getItem() instanceof DyeItem;
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        this.addSlot(new Slot(workstationTileEntity, 1, 193, 30)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack)
            {
                return stack.getItem() instanceof EngineItem;
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        this.addSlot(new Slot(workstationTileEntity, 2, 213, 30)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack)
            {
                return stack.getItem() instanceof WheelItem;
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 102 + y * 18));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 160));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn)
    {
        return true;
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player playerIn, int index)
    {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();

            if(index < 3)
            {
                if(!this.moveItemStackTo(slotStack, 3, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                if(slotStack.getItem() instanceof DyeItem)
                {
                    if(!this.moveItemStackTo(slotStack, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(slotStack.getItem() instanceof EngineItem)
                {
                    if(!this.moveItemStackTo(slotStack, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(slotStack.getItem() instanceof WheelItem)
                {
                    if(!this.moveItemStackTo(slotStack, 2, 3, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(index < 31)
                {
                    if(!this.moveItemStackTo(slotStack, 31, 39, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if(index < 39 && !this.moveItemStackTo(slotStack, 3, 31, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if(slotStack.getCount() == stack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return stack;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public WorkstationBlockEntity getTileEntity()
    {
        return workstationTileEntity;
    }
}
