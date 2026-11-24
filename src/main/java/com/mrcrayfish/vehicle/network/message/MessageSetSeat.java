package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageSetSeat extends PlayMessage<MessageSetSeat>
{
    private int index;

    // Default constructor for deserialization
    public MessageSetSeat() {}

    // Constructor to create a new message
    public MessageSetSeat(int index)
    {
        this.index = index;
    }

    // Encoding the message
    @Override
    public void encode(MessageSetSeat message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.index);
    }

    // Decoding the message
    @Override
    public MessageSetSeat decode(FriendlyByteBuf buffer)
    {
        int index = buffer.readInt();
        return new MessageSetSeat(index);
    }

    // Handling the message
    @Override
    public void handle(MessageSetSeat message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleSetSeatMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the seat index
    public int getIndex()
    {
        return this.index;
    }
}
