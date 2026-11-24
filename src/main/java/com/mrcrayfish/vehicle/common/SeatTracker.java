package com.mrcrayfish.vehicle.common;

import com.google.common.collect.HashBiMap;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncPlayerSeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class SeatTracker
{
    private final HashBiMap<UUID, Integer> playerSeatMap = HashBiMap.create();
    private final WeakReference<VehicleEntity> vehicleRef;

    public SeatTracker(VehicleEntity entity)
    {
        this.vehicleRef = new WeakReference<>(entity);
    }

    public int getSeatIndex(UUID uuid)
    {
        if(this.playerSeatMap.containsKey(uuid))
        {
            return this.playerSeatMap.getOrDefault(uuid, -1);
        }
        return -1;
    }

    private int getMaxSeatSize()
    {
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null)
        {
            return vehicle.getProperties().getSeats().size();
        }
        return 0;
    }

    /**
     * Sets the seat index for the corresponding player uuid. If the uuid already exists
     * in the seating map, it will automatically be updated to the new index.
     *
     * @param index the index of the seat
     * @param uuid the uuid of the player
     */
    public void setSeatIndex(int index, UUID uuid)
    {
        if(index < 0 || index >= this.getMaxSeatSize())
            return;
        this.playerSeatMap.forcePut(uuid, index);
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null && !vehicle.level().isClientSide)
        {
            PacketHandler.getPlayChannel().sendToTracking(() -> vehicle, new MessageSyncPlayerSeat(vehicle.getId(), index, uuid));

            //FrameworkNetwork.sendToTracking(
                    //new MessageSyncPlayerSeat(vehicle.getId(), index, uuid),
                  //  vehicle
           // );

        }
    }

    public boolean isSeatAvailable(int index)
    {
        if(index < 0 || index >= this.getMaxSeatSize())
            return false;
        if(!this.playerSeatMap.inverse().containsKey(index))
            return true;
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null)
        {
            UUID uuid = this.playerSeatMap.inverse().get(index);
            return vehicle.getPassengers().stream().noneMatch(entity -> entity.getUUID().equals(uuid));
        }
        return false;
    }

    public void remove(UUID uuid)
    {
        this.playerSeatMap.remove(uuid);
    }

    public int getNextAvailableSeat()
    {
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null && !vehicle.level().isClientSide)
        {
            VehicleProperties properties = vehicle.getProperties();
            List<Seat> seats = properties.getSeats();

            for(int i = 0; i < seats.size(); i++)
            {
                if(!this.playerSeatMap.containsValue(i))
                {
                    return i;
                }

                UUID uuid = this.playerSeatMap.inverse().get(i);
                if(vehicle.getPassengers().stream().noneMatch(entity -> entity.getUUID().equals(uuid)))
                {
                    this.playerSeatMap.remove(uuid);
                    return i;
                }
            }
        }
        return -1;
    }

    public int getClosestAvailableSeatToPlayer(Player player)
    {
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null && !vehicle.level().isClientSide)
        {
            VehicleProperties properties = vehicle.getProperties();
            List<Seat> seats = properties.getSeats();

            /* If vehicle is full of passengers, no need to search */
            if(vehicle.getPassengers().size() == seats.size())
                return -1;

            int closestSeatIndex = -1;
            double closestDistance = 0;
            for(int i = 0; i < seats.size(); i++)
            {
                if(!this.isSeatAvailable(i))
                    continue;

                /* Get the real world distance to the seat and check if it's the closest */
                Seat seat = seats.get(i);
                Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).multiply(-1, 1, 1).scale(0.0625);
                seatVec = seatVec.yRot(-(vehicle.getYRot()) * 0.017453292F);
                seatVec = seatVec.add(vehicle.position());
                double distance = player.distanceToSqr(seatVec.x, seatVec.y - player.getBbHeight() / 2F, seatVec.z);
                if(closestSeatIndex == -1 || distance < closestDistance)
                {
                    closestSeatIndex = i;
                    closestDistance = distance;
                }
            }
            return closestSeatIndex;
        }
        return -1;
    }

    public CompoundTag write()
    {
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        this.playerSeatMap.forEach((uuid, seatIndex) -> {
            CompoundTag seatTag = new CompoundTag();
            seatTag.putUUID("uuid", uuid);
            seatTag.putInt("seatIndex", seatIndex);
            list.add(seatTag);
        });

        compound.put("playerSeatMap", list);
        return compound;
    }

    public void read(CompoundTag compound)
    {
        if(compound.contains("playerSeatMap", ListTag.TAG_LIST))
        {
            this.playerSeatMap.clear();

            ListTag list = compound.getList("playerSeatMap", CompoundTag.TAG_COMPOUND);
            list.forEach(nbt -> {
                CompoundTag seatTag = (CompoundTag) nbt;
                UUID uuid = seatTag.getUUID("uuid");
                int seatIndex = seatTag.getInt("seatIndex");
                this.playerSeatMap.put(uuid, seatIndex);
            });
        }
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.playerSeatMap.size());

        this.playerSeatMap.forEach((uuid, seatIndex) -> {
            buffer.writeUUID(uuid);
            buffer.writeVarInt(seatIndex);
        });
    }

    public void read(FriendlyByteBuf buffer)
    {
        this.playerSeatMap.clear();

        int size = buffer.readVarInt();
        for(int i = 0; i < size; i++)
        {
            UUID uuid = buffer.readUUID();
            int seatIndex = buffer.readVarInt();
            this.playerSeatMap.put(uuid, seatIndex);
        }
    }
}
