package com.mrcrayfish.vehicle.entity.properties;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class VehiclePropertiesDataLoader extends SimplePreparableReloadListener<Map<ResourceLocation, VehicleProperties>>
{
    private static final VehiclePropertiesDataLoader instance = new VehiclePropertiesDataLoader();

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(VehicleProperties.class, new VehicleProperties.Serializer()).create();

    private static final String PROPERTIES_DIRECTORY = "vehicles/properties";
    private static final String COSMETICS_DIRECTORY = "vehicles/cosmetics";
    private static final String FILE_SUFFIX = ".json";

    private final Map<ResourceLocation, VehicleProperties> vehicleProperties = new HashMap<>();

    @Override
    @NotNull
    protected Map<ResourceLocation, VehicleProperties> prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler)
    {
        Map<ResourceLocation, VehicleProperties> propertiesMap = new HashMap<>();

        for(Map.Entry<ResourceLocation, Resource> entry : manager.listResources(PROPERTIES_DIRECTORY, location -> location.getPath().endsWith(FILE_SUFFIX)).entrySet())
        {
            if(VehicleProperties.DEFAULT_VEHICLE_PROPERTIES.containsKey(format(entry.getKey(), PROPERTIES_DIRECTORY)))
            {
                ResourceLocation key = entry.getKey();

                try
                {
                    InputStream stream = entry.getValue().open();

                    VehicleProperties properties = loadPropertiesFromStream(stream);
                    propertiesMap.put(format(key, PROPERTIES_DIRECTORY), properties);
                }
                catch(IOException ex)
                {
                    VehicleMod.LOGGER.error("Couldn't parse vehicle properties {}", key);
                }
            }
        }

        for(Map.Entry<ResourceLocation, VehicleProperties> entry : propertiesMap.entrySet())
        {
            ResourceLocation key = entry.getKey();
            VehicleProperties properties = entry.getValue();

            if(!properties.getCosmetics().isEmpty())
            {
                // Loads the cosmetics json for applicable vehicles
                Map<ResourceLocation, List<Pair<ResourceLocation, List<ResourceLocation>>>> modelMap = new HashMap<>();

                for(Map.Entry<ResourceLocation, Resource> cosmeticEntry : manager.listResources(COSMETICS_DIRECTORY, fileName -> fileName.equals(key.getPath() + FILE_SUFFIX)).entrySet())
                {
                    ResourceLocation cosmeticKey = cosmeticEntry.getKey();

                    ResourceLocation vehicleId = format(cosmeticKey, COSMETICS_DIRECTORY);
                    if(vehicleId.getNamespace().equals(key.getNamespace()))
                    {
                        CosmeticProperties.deserializeModels(cosmeticKey, manager, modelMap);
                    }
                }

                // Applies the list of valid model locations to the corresponding cosmetic
                modelMap.forEach((cosmeticId, models) -> {
                    CosmeticProperties cosmetic = properties.getCosmetics().get(cosmeticId);
                    if(cosmetic == null)
                        return;
                    cosmetic.setModelLocations(models.stream().map(Pair::getLeft).collect(Collectors.toList()));
                    cosmetic.setDisabledCosmetics(models.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
                });
            }
        }
        
        return propertiesMap;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, VehicleProperties> data, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler)
    {
        this.vehicleProperties.clear();
        this.vehicleProperties.putAll(data);
    }

    protected static VehicleProperties loadPropertiesFromStream(InputStream is)
    {
        return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), VehicleProperties.class);
    }

    @Nullable
    public Map<ResourceLocation, VehicleProperties> getVehicleProperties()
    {
        return this.vehicleProperties;
    }

    private static ResourceLocation format(ResourceLocation location, String directory)
    {
        return new ResourceLocation(location.getNamespace(), location.getPath().substring(directory.length() + 1, location.getPath().length() - FILE_SUFFIX.length()));
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event)
    {
        event.addListener(new VehiclePropertiesDataLoader()); // I still think this looks ugly
    }

    /**
     * Gets the vehicle properties manager.
     */
    public static VehiclePropertiesDataLoader get()
    {
        return instance;
    }

    public void writeVehicleProperties(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.vehicleProperties.size());

        for(Map.Entry<ResourceLocation, VehicleProperties> entry : this.vehicleProperties.entrySet())
        {
            ResourceLocation id = entry.getKey();
            VehicleProperties properties = entry.getValue();
            String json = GSON.toJson(properties, VehicleProperties.class);

            buffer.writeResourceLocation(id);
            buffer.writeUtf(json);
            writeCosmeticModelLocations(buffer, properties);
        }
    }

    public static ImmutableMap<ResourceLocation, VehicleProperties> readVehicleProperties(FriendlyByteBuf buffer)
    {
        int size = buffer.readVarInt();

        if(size > 0)
        {
            ImmutableMap.Builder<ResourceLocation, VehicleProperties> builder = ImmutableMap.builder();
            for(int i = 0; i < size; i++)
            {
                ResourceLocation id = buffer.readResourceLocation();
                String json = buffer.readUtf();

                VehicleProperties properties = GSON.fromJson(json, VehicleProperties.class);
                builder.put(id, properties);

                readCosmeticModelLocations(buffer, properties);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    private static void writeCosmeticModelLocations(FriendlyByteBuf buffer, VehicleProperties properties)
    {
        buffer.writeInt(properties.getCosmetics().size());
        properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) ->
        {
            buffer.writeResourceLocation(cosmeticId);
            buffer.writeInt(cosmeticProperties.getModelLocations().size());
            cosmeticProperties.getModelLocations().forEach(location ->
            {
                buffer.writeResourceLocation(location);
                List<ResourceLocation> disabledCosmetics = cosmeticProperties.getDisabledCosmetics().get(location);
                buffer.writeInt(disabledCosmetics.size());

                disabledCosmetics.forEach(buffer::writeResourceLocation);
            });
        });
    }

    private static void readCosmeticModelLocations(FriendlyByteBuf buffer, VehicleProperties properties)
    {
        int cosmeticsLength = buffer.readInt();

        for(int i = 0; i < cosmeticsLength; i++)
        {
            List<Pair<ResourceLocation, List<ResourceLocation>>> models = new ArrayList<>();
            ResourceLocation cosmeticId = buffer.readResourceLocation();
            int modelsLength = buffer.readInt();
            for(int j = 0; j < modelsLength; j++)
            {
                ResourceLocation modelLocation = buffer.readResourceLocation();
                List<ResourceLocation> disabledCosmetics = new ArrayList<>();
                int disabledCosmeticsLength = buffer.readInt();
                for(int k = 0; k < disabledCosmeticsLength; k++)
                {
                    disabledCosmetics.add(buffer.readResourceLocation());
                }
                models.add(Pair.of(modelLocation, disabledCosmetics));
            }
            Optional.ofNullable(properties.getCosmetics().get(cosmeticId)).ifPresent(cosmetic ->
            {
                cosmetic.setModelLocations(models.stream().map(Pair::getLeft).collect(Collectors.toList()));
                cosmetic.setDisabledCosmetics(models.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
            });
        }
    }
}
