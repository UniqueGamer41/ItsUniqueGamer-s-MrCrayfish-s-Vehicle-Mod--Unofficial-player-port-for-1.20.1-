package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageThrowVehicle extends PlayMessage<MessageThrowVehicle>
{
    // Default constructor for deserialization
    public MessageThrowVehicle() {}

    // Encoding the message (no data to encode)
    @Override
    public void encode(MessageThrowVehicle message, FriendlyByteBuf buffer) {}

    // Decoding the message (no data to decode)
    @Override
    public MessageThrowVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageThrowVehicle();
    }

    // Handling the message
    @Override
    public void handle(MessageThrowVehicle message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleThrowVehicle(context.getPlayer(), message);
        });
        context.setHandled(true);
    }
}
