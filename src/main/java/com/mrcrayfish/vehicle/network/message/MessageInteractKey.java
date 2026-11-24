package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * Author: MrCrayfish
 */
public class MessageInteractKey extends PlayMessage<MessageInteractKey>
{
    private int entityId;

    // Default constructor for deserialization
    public MessageInteractKey() {}

    // Constructor to create a new message
    public MessageInteractKey(Entity targetEntity)
    {
        this.entityId = targetEntity.getId();
    }

    // Constructor for internal use
    private MessageInteractKey(int entityId)
    {
        this.entityId = entityId;
    }

    // Encoding the message
    @Override
    public void encode(MessageInteractKey message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
    }

    // Decoding the message
    @Override
    public MessageInteractKey decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        return new MessageInteractKey(entityId);
    }

    // Handling the message
    @Override
    public void handle(MessageInteractKey message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleInteractKeyMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }
}
