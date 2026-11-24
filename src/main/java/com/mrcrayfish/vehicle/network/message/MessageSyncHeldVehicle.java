package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageSyncHeldVehicle extends PlayMessage<MessageSyncHeldVehicle>
{
    private int entityId;
    private CompoundTag vehicleTag;

    // Default constructor for deserialization
    public MessageSyncHeldVehicle() {}

    // Constructor to create a new message
    public MessageSyncHeldVehicle(int entityId, CompoundTag vehicleTag)
    {
        this.entityId = entityId;
        this.vehicleTag = vehicleTag;
    }

    // Encoding the message
    @Override
    public void encode(MessageSyncHeldVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeNbt(message.vehicleTag);
    }

    // Decoding the message
    @Override
    public MessageSyncHeldVehicle decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readVarInt();
        CompoundTag vehicleTag = buffer.readNbt();
        return new MessageSyncHeldVehicle(entityId, vehicleTag);
    }

    // Handling the message
    @Override
    public void handle(MessageSyncHeldVehicle message, MessageContext context)
    {
        context.execute(() -> {
            ClientPlayHandler.handleSyncHeldVehicle(message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the vehicle tag
    public CompoundTag getVehicleTag()
    {
        return this.vehicleTag;
    }
}
