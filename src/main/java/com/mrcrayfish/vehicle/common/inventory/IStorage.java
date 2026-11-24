package com.mrcrayfish.vehicle.common.inventory;

import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public interface IStorage
{
    Map<String, StorageInventory> getStorageInventories();

    @Nullable
    default StorageInventory getStorageInventory(String key)
    {
        return this.getStorageInventories().get(key);
    }

    default void readInventories(CompoundTag tag)
    {
        CompoundTag storageTag = tag.getCompound("Storage");
        this.getStorageInventories().forEach((key, storage) -> {
            InventoryUtil.readInventoryToNBT(storageTag, key, storage);
        });
    }

    default void writeInventories(CompoundTag tag)
    {
        CompoundTag storageTag = new CompoundTag();
        this.getStorageInventories().forEach((key, storage) -> {
            InventoryUtil.writeInventoryToNBT(storageTag, key, storage);
        });
        tag.put("Storage", storageTag);
    }

    static <T extends VehicleEntity & IStorage> void openStorage(ServerPlayer player, T storage, String key)
    {
        StorageInventory inventory = storage.getStorageInventory(key);
        if(inventory == null)
            return;

        NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> {
            return new StorageContainer(windowId, playerInventory, inventory, playerEntity);
        }, inventory.getDisplayName()), buffer -> {
            buffer.writeVarInt(storage.getId());
            buffer.writeUtf(key);
        });
    }
}
