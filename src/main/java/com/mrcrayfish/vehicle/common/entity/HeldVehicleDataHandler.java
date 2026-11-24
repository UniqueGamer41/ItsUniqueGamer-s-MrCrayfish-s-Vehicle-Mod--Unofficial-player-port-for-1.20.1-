package com.mrcrayfish.vehicle.common.entity;

import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncHeldVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Author: MrCrayfish
 */
public class HeldVehicleDataHandler
{
    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new HeldVehicleDataHandler());
    }

    public static boolean isHoldingVehicle(Player player)
    {
        return !SyncedEntityData.instance().get(player, ModDataKeys.HELD_VEHICLE).isEmpty();
    }

    public static CompoundTag getHeldVehicle(Player player)
    {
        return SyncedEntityData.instance().get(player, ModDataKeys.HELD_VEHICLE);
    }

    public static void setHeldVehicle(Player player, CompoundTag vehicleTag)
    {
        SyncedEntityData.instance().set(player, ModDataKeys.HELD_VEHICLE, vehicleTag);

        if(!player.level().isClientSide)
        {
          //  PacketHandler.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath())
            return;

        CompoundTag vehicleTag = getHeldVehicle(event.getOriginal());
        if(!vehicleTag.isEmpty())
        {
            setHeldVehicle(event.getOriginal(), vehicleTag);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        if(event.getTarget() instanceof Player player)
        {
            CompoundTag vehicleTag = getHeldVehicle(player);
            //PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getTarget()), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinLevelEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Player player && !event.getLevel().isClientSide)
        {
            CompoundTag vehicleTag = getHeldVehicle(player);
           // PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }
}
