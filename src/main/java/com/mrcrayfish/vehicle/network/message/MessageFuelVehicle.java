package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class MessageFuelVehicle extends PlayMessage<MessageFuelVehicle>
{
    private int entityId;
    private InteractionHand hand;

    // Default constructor for deserialization
    public MessageFuelVehicle() {}

    // Constructor to create a new message
    public MessageFuelVehicle(int entityId, InteractionHand hand)
    {
        this.entityId = entityId;
        this.hand = hand;
    }

    // Encoding the message
    @Override
    public void encode(MessageFuelVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeEnum(message.hand);
    }

    // Decoding the message
    @Override
    public MessageFuelVehicle decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        return new MessageFuelVehicle(entityId, hand);
    }

    // Handling the message
    @Override
    public void handle(MessageFuelVehicle message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleFuelVehicleMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the InteractionHand
    public InteractionHand getHand()
    {
        return this.hand;
    }
}
