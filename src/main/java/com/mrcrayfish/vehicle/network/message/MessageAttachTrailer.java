package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageAttachTrailer extends PlayMessage<MessageAttachTrailer>
{
    private int trailerId;

    // Default constructor for deserialization
    public MessageAttachTrailer() {}

    // Constructor to create a new message
    public MessageAttachTrailer(int trailerId)
    {
        this.trailerId = trailerId;
    }

    // Encoding the message
    @Override
    public void encode(MessageAttachTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.trailerId);
    }

    // Decoding the message
    @Override
    public MessageAttachTrailer decode(FriendlyByteBuf buffer)
    {
        int trailerId = buffer.readInt();
        return new MessageAttachTrailer(trailerId);
    }

    // Handling the message
    @Override
    public void handle(MessageAttachTrailer message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleAttachTrailerMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the trailer ID
    public int getTrailerId()
    {
        return this.trailerId;
    }
}
