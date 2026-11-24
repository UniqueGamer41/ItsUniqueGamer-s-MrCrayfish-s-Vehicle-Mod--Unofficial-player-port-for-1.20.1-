package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class MessageInteractCosmetic extends PlayMessage<MessageInteractCosmetic>
{
    private int entityId;
    private ResourceLocation cosmeticId;

    // Default constructor for deserialization
    public MessageInteractCosmetic() {}

    // Constructor to create a new message
    public MessageInteractCosmetic(int entityId, ResourceLocation cosmeticId)
    {
        this.entityId = entityId;
        this.cosmeticId = cosmeticId;
    }

    // Encoding the message
    @Override
    public void encode(MessageInteractCosmetic message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeResourceLocation(message.cosmeticId);
    }

    // Decoding the message
    @Override
    public MessageInteractCosmetic decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        ResourceLocation cosmeticId = buffer.readResourceLocation();
        return new MessageInteractCosmetic(entityId, cosmeticId);
    }

    // Handling the message
    @Override
    public void handle(MessageInteractCosmetic message, MessageContext context)
    {
        context.execute(() -> {
            ServerPlayHandler.handleInteractCosmeticMessage(context.getPlayer(), message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the cosmetic ID
    public ResourceLocation getCosmeticId()
    {
        return this.cosmeticId;
    }
}
