package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * Author: MrCrayfish
 */
public class MessagePickupVehicle extends PlayMessage<MessagePickupVehicle>
{
    private int entityId;

    // Default constructor for deserialization
    public MessagePickupVehicle() {}

    // Constructor to create a new message from an entity
    public MessagePickupVehicle(Entity targetEntity)
    {
        this.entityId = targetEntity.getId();
    }

    // Constructor to create a new message from an entity ID
    public MessagePickupVehicle(int entityId)
    {
        this.entityId = entityId;
    }

    // Encoding the message
    @Override
    public void encode(MessagePickupVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
    }

    // Decoding the message
    @Override
    public MessagePickupVehicle decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        return new MessagePickupVehicle(entityId);
    }

    // Handling the message
    @Override
    public void handle(MessagePickupVehicle message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handlePickupVehicleMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }
}
