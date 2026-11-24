package com.mrcrayfish.vehicle.network.play;

import com.mrcrayfish.vehicle.common.CosmeticTracker;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.message.MessageEntityFluid;
import com.mrcrayfish.vehicle.network.message.MessageSyncActionData;
import com.mrcrayfish.vehicle.network.message.MessageSyncCosmetics;
import com.mrcrayfish.vehicle.network.message.MessageSyncHeldVehicle;
import com.mrcrayfish.vehicle.network.message.MessageSyncPlayerSeat;
import com.mrcrayfish.vehicle.network.message.MessageSyncStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ClientPlayHandler
{
    public static void handleSyncStorage(MessageSyncStorage message)
    {
        Level world = Minecraft.getInstance().level;
        if(world == null)
            return;

        Entity entity = world.getEntity(message.getEntityId());
        if(!(entity instanceof IStorage storage))
            return;

        String[] keys = message.getKeys();
        CompoundTag[] tags = message.getTags();
        for(int i = 0; i < keys.length; i++)
        {
            StorageInventory inventory = storage.getStorageInventory(keys[i]);
            if(inventory != null)
            {
                CompoundTag tag = tags[i];
                inventory.fromTag(tag.getList("Inventory", Tag.TAG_COMPOUND));
            }
        }
    }

    public static void handleEntityFluid(MessageEntityFluid message)
    {
        Level world = Minecraft.getInstance().level;
        if(world == null)
            return;

        Entity entity = world.getEntity(message.getEntityId());
        if(entity == null)
            return;

        LazyOptional<IFluidHandler> optional = entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
        optional.ifPresent(handler ->
        {
            if(handler instanceof FluidTank tank)
            {
                tank.setFluid(message.getStack());
            }
        });
    }

    public static void handleSyncPlayerSeat(MessageSyncPlayerSeat message)
    {
        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            Entity entity = player.getCommandSenderWorld().getEntity(message.getEntityId());
            if(entity instanceof VehicleEntity vehicle)
            {
                int oldSeatIndex = vehicle.getSeatTracker().getSeatIndex(message.getUuid());
                vehicle.getSeatTracker().setSeatIndex(message.getSeatIndex(), message.getUuid());
                Entity passenger = vehicle.getPassengers().stream().filter(e -> e.getUUID().equals(message.getUuid())).findFirst().orElse(null);
                if(passenger instanceof Player)
                {
                    vehicle.onPlayerChangeSeat((Player) passenger, oldSeatIndex, message.getSeatIndex());
                }
            }
        }
    }

    public static void handleSyncHeldVehicle(MessageSyncHeldVehicle message)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            Entity entity = world.getEntity(message.getEntityId());
            if(entity instanceof Player)
            {
                HeldVehicleDataHandler.setHeldVehicle((Player) entity, message.getVehicleTag());
            }
        }
    }

    public static void handleSyncCosmetics(MessageSyncCosmetics message)
    {
        Level world = Minecraft.getInstance().level;
        if(world == null)
            return;

        Entity entity = world.getEntity(message.getEntityId());
        if(!(entity instanceof VehicleEntity))
            return;

        CosmeticTracker tracker = ((VehicleEntity) entity).getCosmeticTracker();
        message.getDirtyEntries().forEach(pair -> tracker.setSelectedModel(pair.getLeft(), pair.getRight()));
    }

    public static void handleSyncActionData(MessageSyncActionData message)
    {
        Level world = Minecraft.getInstance().level;
        if(world == null)
            return;

        Entity entity = world.getEntity(message.getEntityId());
        if(!(entity instanceof VehicleEntity))
            return;

        CosmeticTracker tracker = ((VehicleEntity) entity).getCosmeticTracker();
        tracker.getSelectedCosmeticEntry(message.getCosmeticId()).ifPresent(entry -> {
            message.getActionData().forEach(pair -> {
                entry.getAction(pair.getLeft()).ifPresent(action -> {
                    action.load(pair.getRight(), true);
                });
            });
        });
    }
}
