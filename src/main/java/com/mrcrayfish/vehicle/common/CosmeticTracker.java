package com.mrcrayfish.vehicle.common;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticActions;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncActionData;
import com.mrcrayfish.vehicle.network.message.MessageSyncCosmetics;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class CosmeticTracker
{
    private final ImmutableMap<ResourceLocation, Entry> selectedCosmetics;
    private final Object2ObjectMap<ResourceLocation, List<Action>> dirtyActions = Util.make(() -> {
        Object2ObjectMap<ResourceLocation, List<Action>> map = new Object2ObjectOpenHashMap<>();
        map.defaultReturnValue(new ObjectArrayList<>());
        return map;
    });
    private final WeakReference<VehicleEntity> vehicleRef;
    private boolean dirty = false;

    public CosmeticTracker(VehicleEntity vehicle)
    {
        this.vehicleRef = new WeakReference<>(vehicle);
        ImmutableMap.Builder<ResourceLocation, Entry> builder = ImmutableMap.builder();
        vehicle.getProperties().getCosmetics().forEach((cosmeticId, cosmeticProperties) -> {
            builder.put(cosmeticId, new Entry(cosmeticProperties));
        });
        this.selectedCosmetics = builder.build();
    }

    public void tick(VehicleEntity vehicle)
    {
        if(!vehicle.level().isClientSide() && this.dirty)
        {
            PacketHandler.getPlayChannel().sendToTracking(() -> vehicle, new MessageSyncCosmetics(vehicle.getId(), this.getDirtyEntries()));
            this.resetDirty();
        }

        this.selectedCosmetics.forEach((cosmeticId, entry) ->
        {
            entry.getActions().forEach(action ->
            {
                action.tick(vehicle);
                if(!vehicle.level().isClientSide() && action.isDirty())
                {
                    this.dirtyActions.get(cosmeticId).add(action);
                }
            });
        });

        if(!vehicle.level().isClientSide())
        {
            if(!this.dirtyActions.isEmpty())
            {
                this.dirtyActions.forEach((cosmeticId, actions) ->
                {
                    List<Pair<ResourceLocation, CompoundTag>> actionData = actions.stream().map(action -> Pair.of(CosmeticActions.getId(action.getClass()), action.save(true))).collect(Collectors.toList());
                    PacketHandler.getPlayChannel().sendToTracking(() -> vehicle, new MessageSyncActionData(vehicle.getId(), cosmeticId, actionData));
                    actions.forEach(Action::clean);
                });
                this.dirtyActions.clear();
            }
        }
    }

    public Optional<Entry> getSelectedCosmeticEntry(ResourceLocation cosmeticId)
    {
        return Optional.ofNullable(this.selectedCosmetics.get(cosmeticId));
    }

    public void setSelectedModel(ResourceLocation cosmeticId, ResourceLocation modelLocation)
    {
        if(FMLLoader.isProduction() && !this.isValidCosmeticModel(cosmeticId, modelLocation))
        {
            return;
        }

        Entry entry = this.selectedCosmetics.get(cosmeticId);
        if(entry != null)
        {
            entry.setModelLocation(modelLocation);
        }
        this.dirty = true;
    }

    private boolean isValidCosmeticModel(ResourceLocation cosmeticId, ResourceLocation modelLocation)
    {
        VehicleEntity vehicle = this.vehicleRef.get();
        if(vehicle != null)
        {
            CosmeticProperties properties = vehicle.getProperties().getCosmetics().get(cosmeticId);
            return properties != null && properties.getModelLocations().contains(modelLocation);
        }
        return false;
    }

    @Nullable
    public ResourceLocation getSelectedModelLocation(ResourceLocation cosmeticId)
    {
        Entry entry = this.selectedCosmetics.get(cosmeticId);
        if(entry != null)
        {
            return entry.getModelLocation();
        }

        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public ComponentModel getSelectedModel(ResourceLocation cosmeticId)
    {
        Entry entry = this.selectedCosmetics.get(cosmeticId);
        if(entry != null)
        {
            return entry.getComponentModel();
        }

        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Entry getSelectedEntry(ResourceLocation cosmeticId)
    {
        return this.selectedCosmetics.get(cosmeticId);
    }

    public Collection<Action> getActions(ResourceLocation cosmeticId)
    {
        Entry entry = this.selectedCosmetics.get(cosmeticId);
        if(entry != null)
        {
            return entry.getActions();
        }

        return Collections.emptyList();
    }

    private List<Pair<ResourceLocation, ResourceLocation>> getDirtyEntries()
    {
        List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries = new ObjectArrayList<>();
        this.selectedCosmetics.forEach((cosmeticId, entry) ->
        {
            if(entry.dirty)
            {
                dirtyEntries.add(Pair.of(cosmeticId, entry.getModelLocation()));
            }
        });
        return dirtyEntries;
    }

    private void resetDirty()
    {
        this.dirty = false;
        this.selectedCosmetics.forEach((cosmeticId, entry) -> entry.dirty = false);
    }

    public CompoundTag write()
    {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        this.selectedCosmetics.forEach((cosmeticId, entry) -> {
            CompoundTag cosmeticTag = new CompoundTag();
            cosmeticTag.putString("Id", cosmeticId.toString());
            cosmeticTag.putString("Model", entry.getModelLocation().toString());
            CompoundTag actions = new CompoundTag();
            entry.getActions().forEach(action -> {
                ResourceLocation id = CosmeticActions.getId(action.getClass());
                actions.put(id.toString(), action.save(false));
            });
            cosmeticTag.put("Actions", actions);
            list.add(cosmeticTag);
        });
        tag.put("Cosmetics", list);
        return tag;
    }

    public void read(CompoundTag tag)
    {
        if(tag.contains("Cosmetics", Tag.TAG_LIST))
        {
            ListTag list = tag.getList("Cosmetics", Tag.TAG_COMPOUND);
            list.forEach(nbt -> {
                CompoundTag cosmeticTag = (CompoundTag) nbt;
                ResourceLocation cosmeticId = new ResourceLocation(cosmeticTag.getString("Id"));
                ResourceLocation modelLocation = new ResourceLocation(cosmeticTag.getString("Model"));
                this.setSelectedModel(cosmeticId, modelLocation);
                CompoundTag actions = cosmeticTag.getCompound("Actions");
                this.selectedCosmetics.get(cosmeticId).getActions().forEach(action -> {
                    ResourceLocation id = CosmeticActions.getId(action.getClass());
                    action.load(actions.getCompound(id.toString()), false);
                });
            });
        }
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.selectedCosmetics.size());
        this.selectedCosmetics.forEach((cosmeticId, entry) -> {
            buffer.writeResourceLocation(cosmeticId);
            buffer.writeResourceLocation(entry.getModelLocation());
            buffer.writeInt(entry.getActions().size());
            entry.getActions().forEach(action -> {
                buffer.writeResourceLocation(CosmeticActions.getId(action.getClass()));
                buffer.writeNbt(action.save(false));
            });
        });
    }

    public void read(FriendlyByteBuf buffer)
    {
        int size = buffer.readInt();
        for(int i = 0; i < size; i++)
        {
            ResourceLocation cosmeticId = buffer.readResourceLocation();
            ResourceLocation modelLocation = buffer.readResourceLocation();
            this.setSelectedModel(cosmeticId, modelLocation);
            int actionLength = buffer.readInt();
            if(actionLength > 0)
            {
                Map<ResourceLocation, CompoundTag> dataMap = new HashMap<>();
                for(int j = 0; j < actionLength; j++)
                {
                    ResourceLocation id = buffer.readResourceLocation();
                    CompoundTag data = buffer.readNbt();
                    dataMap.put(id, data);
                }

                this.selectedCosmetics.get(cosmeticId).getActions().forEach(action ->
                {
                    ResourceLocation id = CosmeticActions.getId(action.getClass());
                    CompoundTag data = dataMap.get(id);
                    if(data != null)
                    {
                        action.load(data, false);
                    }
                });
            }
        }
    }

    public static class Entry
    {
        private ResourceLocation modelLocation;
        private final Map<ResourceLocation, Action> actions;
        private boolean dirty;

        @Nullable
        private ComponentModel componentModel; // ComponentModel

        public Entry(CosmeticProperties properties)
        {
            this.modelLocation = properties.getModelLocations().get(0);
            this.actions = ImmutableMap.copyOf(properties.getActions().stream().map(Supplier::get).collect(Collectors.toMap(a -> CosmeticActions.getId(a.getClass()), a -> a)));
        }

        public void setModelLocation(ResourceLocation modelLocation)
        {
            this.modelLocation = modelLocation;
            this.dirty = true;
            this.componentModel = null;
        }

        public ResourceLocation getModelLocation()
        {
            return this.modelLocation;
        }

        @Nullable
        public ComponentModel getComponentModel()
        {
            if(this.componentModel == null)
            {
                this.componentModel = ComponentManager.lookupModel(this.modelLocation);
            }
            return this.componentModel;
        }

        public Collection<Action> getActions()
        {
            return this.actions.values();
        }

        public Optional<Action> getAction(ResourceLocation id)
        {
            return Optional.ofNullable(this.actions.get(id));
        }
    }
}