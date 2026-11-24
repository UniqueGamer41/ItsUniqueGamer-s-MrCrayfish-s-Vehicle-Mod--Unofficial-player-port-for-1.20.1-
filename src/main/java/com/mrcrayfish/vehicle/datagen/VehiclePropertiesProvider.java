package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public abstract class VehiclePropertiesProvider implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final HashFunction HASH = Hashing.sha256();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(VehicleProperties.class, new VehicleProperties.Serializer()).create();

    private final DataGenerator generator;
    private final Map<ResourceLocation, VehicleProperties> vehiclePropertiesMap = new HashMap<>();
    private boolean scaleWheels = false;

    protected VehiclePropertiesProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    public void setScaleWheels(boolean scaleWheels)
    {
        this.scaleWheels = scaleWheels;
    }

    protected final void add(EntityType<? extends VehicleEntity> type, VehicleProperties.Builder builder)
    {
        this.add(ForgeRegistries.ENTITY_TYPES.getKey(type), builder);
    }

    protected final void add(ResourceLocation id, VehicleProperties.Builder builder)
    {
        this.vehiclePropertiesMap.put(id, builder.build(this.scaleWheels));
    }

    public Map<ResourceLocation, VehicleProperties> getVehiclePropertiesMap()
    {
        return ImmutableMap.copyOf(this.vehiclePropertiesMap);
    }

    public abstract void registerProperties();

    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        this.vehiclePropertiesMap.clear();
        this.registerProperties();
        this.vehiclePropertiesMap.forEach((id, properties) ->
        {
            String modId = id.getNamespace();
            String vehicleId = id.getPath();
            Path path = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/properties/" + vehicleId + ".json");
            try
            {
                if(!Files.exists(path))
                {
                    Files.createDirectories(path.getParent());
                }
                DataProvider.saveStable(cache, GSON.toJsonTree(properties), path);
            }
            catch(IOException e)
            {
                LOGGER.error("Couldn't save vehicle properties to {}", path, e);
            }

            if(properties.getCosmetics().isEmpty())
                return;

            path = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/cosmetics/" + vehicleId + ".json");
            try
            {
                JsonObject object = new JsonObject();
                object.addProperty("replace", false);
                JsonObject validModels = new JsonObject();
                properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) ->
                {
                    JsonArray array = new JsonArray();
                    cosmeticProperties.getModelLocations().forEach(location ->
                    {
                        List<ResourceLocation> disabledCosmetics = cosmeticProperties.getDisabledCosmetics().getOrDefault(location, Collections.emptyList());
                        if(disabledCosmetics.isEmpty())
                        {
                            array.add(location.toString());
                        }
                        else
                        {
                            JsonObject modelObject = new JsonObject();
                            modelObject.addProperty("model", location.toString());
                            JsonArray disables = new JsonArray();
                            disabledCosmetics.forEach(disabledCosmeticId -> disables.add(disabledCosmeticId.toString()));
                            modelObject.add("disables", disables);
                            array.add(modelObject);
                        }
                    });
                    validModels.add(cosmeticId.toString(), array);
                });
                object.add("valid_models", validModels);

                if(!Files.exists(path))
                {
                    Files.createDirectories(path.getParent());
                }

                DataProvider.saveStable(cache, object, path);
            }
            catch(IOException e)
            {
                LOGGER.error("Couldn't save vehicle cosmetics to {}", path, e);
            }
        });
        return null;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "VehicleProperties";
    }
}
