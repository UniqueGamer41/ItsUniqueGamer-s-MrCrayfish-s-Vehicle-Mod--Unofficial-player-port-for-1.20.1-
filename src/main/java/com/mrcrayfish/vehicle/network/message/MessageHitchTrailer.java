package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageHitchTrailer extends PlayMessage<MessageHitchTrailer>
{
    private boolean hitch;

    // Default constructor for deserialization
    public MessageHitchTrailer() {}

    // Constructor to create a new message
    public MessageHitchTrailer(boolean hitch)
    {
        this.hitch = hitch;
    }

    // Encoding the message
    @Override
    public void encode(MessageHitchTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.hitch);
    }

    // Decoding the message
    @Override
    public MessageHitchTrailer decode(FriendlyByteBuf buffer)
    {
        boolean hitch = buffer.readBoolean();
        return new MessageHitchTrailer(hitch);
    }

    // Handling the message
    @Override
    public void handle(MessageHitchTrailer message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleHitchTrailerMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for hitch state
    public boolean isHitch()
    {
        return this.hitch;
    }
}
