package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageOpenStorage extends PlayMessage<MessageOpenStorage>
{
    private int entityId;
    private String key;

    // Default constructor for deserialization
    public MessageOpenStorage() {}

    // Constructor to create a new message
    public MessageOpenStorage(int entityId, String key)
    {
        this.entityId = entityId;
        this.key = key;
    }

    // Encoding the message
    @Override
    public void encode(MessageOpenStorage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeUtf(message.key);
    }

    // Decoding the message
    @Override
    public MessageOpenStorage decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        String key = buffer.readUtf();
        return new MessageOpenStorage(entityId, key);
    }

    // Handling the message
    @Override
    public void handle(MessageOpenStorage message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleOpenStorageMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the key
    public String getKey()
    {
        return this.key;
    }
}
