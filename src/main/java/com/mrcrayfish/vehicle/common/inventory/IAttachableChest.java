package com.mrcrayfish.vehicle.common.inventory;

import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public interface IAttachableChest
{
    boolean hasChest(String key);

    void attachChest(String key, ItemStack stack);

    void removeChest(String key);
}
