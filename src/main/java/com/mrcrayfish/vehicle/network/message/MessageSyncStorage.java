package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class MessageSyncStorage extends PlayMessage<MessageSyncStorage>
{
    private int entityId;
    private String[] keys;
    private CompoundTag[] tags;

    // Default constructor for deserialization
    public MessageSyncStorage() {}

    // Constructor to create a new message
    public <T extends VehicleEntity & IStorage> MessageSyncStorage(T vehicle, String... keys)
    {
        this.entityId = vehicle.getId();
        List<Pair<String, CompoundTag>> tagList = new ArrayList<>();
        for (String key : keys)
        {
            StorageInventory inventory = vehicle.getStorageInventory(key);
            if (inventory != null)
            {
                CompoundTag tag = new CompoundTag();
                tag.put("Inventory", inventory.createTag());
                tagList.add(Pair.of(key, tag));
            }
        }
        this.keys = tagList.stream().map(Pair::getLeft).toArray(String[]::new);
        this.tags = tagList.stream().map(Pair::getRight).toArray(CompoundTag[]::new);
    }

    private MessageSyncStorage(int entityId, String[] keys, CompoundTag[] tags)
    {
        this.entityId = entityId;
        this.keys = keys;
        this.tags = tags;
    }

    // Encoding the message
    @Override
    public void encode(MessageSyncStorage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.keys.length);
        for (int i = 0; i < message.keys.length; i++)
        {
            buffer.writeUtf(message.keys[i]);
            buffer.writeNbt(message.tags[i]);
        }
    }

    // Decoding the message
    @Override
    public MessageSyncStorage decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        int keyLength = buffer.readInt();
        String[] keys = new String[keyLength];
        CompoundTag[] tags = new CompoundTag[keyLength];
        for (int i = 0; i < keyLength; i++)
        {
            keys[i] = buffer.readUtf();
            tags[i] = buffer.readNbt();
        }
        return new MessageSyncStorage(entityId, keys, tags);
    }

    // Handling the message
    @Override
    public void handle(MessageSyncStorage message, MessageContext context)
    {
        context.execute(() -> {
            ClientPlayHandler.handleSyncStorage(message);
        });
        context.setHandled(true);
    }

    // Getters
    public int getEntityId()
    {
        return this.entityId;
    }

    public String[] getKeys()
    {
        return this.keys;
    }

    public CompoundTag[] getTags()
    {
        return this.tags;
    }
}
