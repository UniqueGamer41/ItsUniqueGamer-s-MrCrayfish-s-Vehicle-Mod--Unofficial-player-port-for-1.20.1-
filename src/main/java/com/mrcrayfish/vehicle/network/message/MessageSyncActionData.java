package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class MessageSyncActionData extends PlayMessage<MessageSyncActionData>
{
    private int entityId;
    private ResourceLocation cosmeticId;
    private List<Pair<ResourceLocation, CompoundTag>> actionData;

    // Default constructor for deserialization
    public MessageSyncActionData() {}

    // Constructor to create a new message
    public MessageSyncActionData(int entityId, ResourceLocation cosmeticId, List<Pair<ResourceLocation, CompoundTag>> actionData)
    {
        this.entityId = entityId;
        this.cosmeticId = cosmeticId;
        this.actionData = actionData;
    }

    // Encoding the message
    @Override
    public void encode(MessageSyncActionData message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeResourceLocation(message.cosmeticId);
        buffer.writeInt(message.actionData.size());

        message.actionData.forEach(pair -> {
            buffer.writeResourceLocation(pair.getLeft());
            buffer.writeNbt(pair.getRight());
        });
    }

    // Decoding the message
    @Override
    public MessageSyncActionData decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        ResourceLocation cosmeticId = buffer.readResourceLocation();

        List<Pair<ResourceLocation, CompoundTag>> actionData = new ArrayList<>();
        int size = buffer.readInt();

        for (int i = 0; i < size; i++)
        {
            ResourceLocation actionId = buffer.readResourceLocation();
            CompoundTag data = buffer.readNbt();
            actionData.add(Pair.of(actionId, data));
        }
        return new MessageSyncActionData(entityId, cosmeticId, actionData);
    }

    // Handling the message
    @Override
    public void handle(MessageSyncActionData message, MessageContext context)
    {
        context.execute(() -> {
            ClientPlayHandler.handleSyncActionData(message);
        });
        context.setHandled(true);
    }

    // Getters for the data
    public int getEntityId()
    {
        return this.entityId;
    }

    public ResourceLocation getCosmeticId()
    {
        return this.cosmeticId;
    }

    public List<Pair<ResourceLocation, CompoundTag>> getActionData()
    {
        return this.actionData;
    }
}
