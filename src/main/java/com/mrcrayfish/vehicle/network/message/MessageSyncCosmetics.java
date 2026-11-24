package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class MessageSyncCosmetics extends PlayMessage<MessageSyncCosmetics>
{
    private int entityId;
    private List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries;

    // Default constructor for deserialization
    public MessageSyncCosmetics() {}

    // Constructor to create a new message
    public MessageSyncCosmetics(int entityId, List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries)
    {
        this.entityId = entityId;
        this.dirtyEntries = dirtyEntries;
    }

    // Encoding the message
    @Override
    public void encode(MessageSyncCosmetics message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.dirtyEntries.size());

        message.dirtyEntries.forEach(pair -> {
            buffer.writeResourceLocation(pair.getLeft());
            buffer.writeResourceLocation(pair.getRight());
        });
    }

    // Decoding the message
    @Override
    public MessageSyncCosmetics decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            ResourceLocation cosmeticId = buffer.readResourceLocation();
            ResourceLocation modelLocation = buffer.readResourceLocation();
            dirtyEntries.add(Pair.of(cosmeticId, modelLocation));
        }
        return new MessageSyncCosmetics(entityId, dirtyEntries);
    }

    // Handling the message
    @Override
    public void handle(MessageSyncCosmetics message, MessageContext context)
    {
        context.execute(() -> {
            ClientPlayHandler.handleSyncCosmetics(message);
        });
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the dirty entries
    public List<Pair<ResourceLocation, ResourceLocation>> getDirtyEntries()
    {
        return this.dirtyEntries;
    }
}
