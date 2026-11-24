package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class MessageSyncPlayerSeat extends PlayMessage<MessageSyncPlayerSeat>
{
    private int entityId;
    private int seatIndex;
    private UUID uuid;

    // Default constructor for deserialization
    public MessageSyncPlayerSeat() {}

    // Constructor to create a new message
    public MessageSyncPlayerSeat(int entityId, int seatIndex, UUID uuid)
    {
        this.entityId = entityId;
        this.seatIndex = seatIndex;
        this.uuid = uuid;
    }

    // Encoding the message
    @Override
    public void encode(MessageSyncPlayerSeat message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.seatIndex);
        buffer.writeUUID(message.uuid);
    }

    // Decoding the message
    @Override
    public MessageSyncPlayerSeat decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readVarInt();
        int seatIndex = buffer.readVarInt();
        UUID uuid = buffer.readUUID();
        return new MessageSyncPlayerSeat(entityId, seatIndex, uuid);
    }

    // Handling the message
    @Override
    public void handle(MessageSyncPlayerSeat message, MessageContext context)
    {
        context.execute(() -> {
            ClientPlayHandler.handleSyncPlayerSeat(message);
        });
        context.setHandled(true);
    }

    // Getters
    public int getEntityId()
    {
        return this.entityId;
    }

    public int getSeatIndex()
    {
        return this.seatIndex;
    }

    public UUID getUuid()
    {
        return this.uuid;
    }
}
