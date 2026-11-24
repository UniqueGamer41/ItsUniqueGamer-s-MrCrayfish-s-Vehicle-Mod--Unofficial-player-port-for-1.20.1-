package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class MessageCycleSeats extends PlayMessage<MessageCycleSeats>
{
    // Default constructor for deserialization
    public MessageCycleSeats() {}

    // Encoding the message (no data to encode)
    @Override
    public void encode(MessageCycleSeats message, FriendlyByteBuf buffer) {}

    // Decoding the message (no data to decode)
    @Override
    public MessageCycleSeats decode(FriendlyByteBuf buffer)
    {
        return new MessageCycleSeats();
    }

    // Handling the message
    @Override
    public void handle(MessageCycleSeats message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer();
            if (player != null)
            {
                ServerPlayHandler.handleCycleSeatsMessage(player, message);
            }
        });
        context.setHandled(true);
    }
}
