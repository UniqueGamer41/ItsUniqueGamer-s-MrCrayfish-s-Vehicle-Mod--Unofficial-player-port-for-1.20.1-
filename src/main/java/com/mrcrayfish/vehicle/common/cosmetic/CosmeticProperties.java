package com.mrcrayfish.vehicle.common.cosmetic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class CosmeticProperties
{
    public static final Vec3 DEFAULT_OFFSET = Vec3.ZERO;
    public static final Vec3 DEFAULT_ROTATION = Vec3.ZERO;
    public static final float DEFAULT_SCALE = 1.0F;

    private final ResourceLocation id;
    private final Vec3 offset;
    private final Vec3 rotation;
    private final float scale;
    private final List<Supplier<Action>> actions;

    private List<ResourceLocation> modelLocations = new ArrayList<>();
    private Map<ResourceLocation, List<ResourceLocation>> disabledCosmetics = new HashMap<>();

    public CosmeticProperties(ResourceLocation id, Vec3 offset, Vec3 rotation, float scale, List<Supplier<Action>> actions)
    {
        this.id = id;
        this.offset = offset;
        this.rotation = rotation;
        this.scale = scale;
        this.actions = actions;
    }

    public CosmeticProperties(JsonObject object)
    {
        this.id = new ResourceLocation(GsonHelper.getAsString(object, "id"));

        this.offset = ExtraJSONUtils.getAsVec3(object, "offset", DEFAULT_OFFSET);
        this.rotation = ExtraJSONUtils.getAsVec3(object, "rotation", DEFAULT_ROTATION);
        this.scale = GsonHelper.getAsFloat(object, "scale", DEFAULT_SCALE);

        List<Supplier<Action>> actions = new ArrayList<>();

        JsonArray array = GsonHelper.getAsJsonArray(object, "actions", new JsonArray());

        for(int idx = 0; idx < array.size(); idx++)
        {
            JsonElement element = array.get(idx);
            if(element.isJsonObject())
            {
                JsonObject action = element.getAsJsonObject();
                ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(action, "id"));
                Supplier<Action> actionSupplier = CosmeticActions.getSupplier(type, action);
                Objects.requireNonNull(actionSupplier, "Unregistered cosmetic action: " + type);
                actions.add(actionSupplier);
            }
        }

        this.actions = actions;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public Vec3 getOffset()
    {
        return this.offset;
    }

    public Vec3 getRotation()
    {
        return this.rotation;
    }

    public float getScale()
    {
        return this.scale;
    }

    public void setModelLocations(List<ResourceLocation> locations)
    {
        this.modelLocations = ImmutableList.copyOf(locations);
    }

    public List<ResourceLocation> getModelLocations()
    {
        return this.modelLocations;
    }

    public void setDisabledCosmetics(Map<ResourceLocation, List<ResourceLocation>> disabledCosmetics)
    {
        this.disabledCosmetics = ImmutableMap.copyOf(disabledCosmetics);
    }

    public Map<ResourceLocation, List<ResourceLocation>> getDisabledCosmetics()
    {
        return this.disabledCosmetics;
    }

    public List<Supplier<Action>> getActions()
    {
        return this.actions;
    }

    public void serialize(JsonObject object)
    {
        object.addProperty("id", this.id.toString());

        ExtraJSONUtils.write(object, "offset", this.offset, DEFAULT_OFFSET);
        ExtraJSONUtils.write(object, "rotation", this.rotation, DEFAULT_ROTATION);

        if(this.scale != DEFAULT_SCALE)
        {
            object.addProperty("scale", this.scale);
        }

        if(this.actions.isEmpty())
            return;

        JsonArray actions = new JsonArray();

        this.actions.forEach(actionSupplier -> {
            Action action = actionSupplier.get();
            ResourceLocation id = CosmeticActions.getId(action.getClass());

            if(id == null)
                return;

            JsonObject actionData = new JsonObject();
            actionData.addProperty("id", id.toString());
            action.serialize(actionData);
            actions.add(actionData);
        });

        object.add("actions", actions);
    }

    public static void deserializeModels(ResourceLocation cosmeticLocation, ResourceManager manager, Map<ResourceLocation, List<Pair<ResourceLocation, List<ResourceLocation>>>> modelMap)
    {
        try
        {
            Resource resource = manager.getResourceOrThrow(cosmeticLocation);

            deserializeModels(resource.open(), modelMap);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void deserializeModels(InputStream is, Map<ResourceLocation, List<Pair<ResourceLocation, List<ResourceLocation>>>> modelMap)
    {
        JsonObject object = GsonHelper.parse(new InputStreamReader(is, StandardCharsets.UTF_8));

        boolean replace = GsonHelper.getAsBoolean(object, "replace", false);
        if(replace)
            modelMap.clear();

        JsonObject validModelsObject = GsonHelper.getAsJsonObject(object, "valid_models", new JsonObject());

        validModelsObject.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonArray())
                .forEach(entry ->
                {
                    JsonArray modelArray = entry.getValue().getAsJsonArray();
                    ResourceLocation cosmeticId = new ResourceLocation(entry.getKey());

                    modelArray.forEach(modelElement ->
                    {
                        if(modelElement.isJsonPrimitive() && modelElement.getAsJsonPrimitive().isString())
                        {
                            ResourceLocation location = new ResourceLocation(modelElement.getAsString());

                            modelMap.computeIfAbsent(cosmeticId, id -> new ArrayList<>())
                                    .add(Pair.of(location, Collections.emptyList()));
                        }
                        else if(modelElement.isJsonObject())
                        {
                            JsonObject modelObject = modelElement.getAsJsonObject();

                            ResourceLocation location =
                                    new ResourceLocation(GsonHelper.getAsString(modelObject, "model"));

                            JsonArray disabledArray =
                                    GsonHelper.getAsJsonArray(modelObject, "disables", new JsonArray());

                            List<ResourceLocation> disabledCosmetics = new ArrayList<>();

                            for(int idx = 0; idx < disabledArray.size(); idx++)
                            {
                                JsonElement element = disabledArray.get(idx);

                                if(element.isJsonPrimitive())
                                {
                                    JsonPrimitive primitive = element.getAsJsonPrimitive();

                                    if(primitive.isString())
                                    {
                                        disabledCosmetics.add(
                                                new ResourceLocation(primitive.getAsString())
                                        );
                                    }
                                }
                            }

                            modelMap.computeIfAbsent(cosmeticId, id -> new ArrayList<>())
                                    .add(Pair.of(location, disabledCosmetics));
                        }
                        else
                        {
                            throw new JsonSyntaxException(
                                    "Expected a string or an object for cosmetic model definition"
                            );
                        }
                    });
                });
    }

    public static Builder builder(ResourceLocation id)
    {
        return new Builder(id);
    }

    public static class Builder
    {
        private final ResourceLocation id;

        private Vec3 offset = DEFAULT_OFFSET;
        private Vec3 rotation = DEFAULT_ROTATION;
        private float scale = DEFAULT_SCALE;

        private List<ResourceLocation> models = new ArrayList<>();
        private Map<ResourceLocation, List<ResourceLocation>> disabledCosmetics = new HashMap<>();
        private List<Supplier<Action>> actions = new ArrayList<>();

        public Builder(ResourceLocation id)
        {
            this.id = id;
        }

        public Builder setOffset(Vec3 offset)
        {
            this.offset = offset;
            return this;
        }

        public Builder setOffset(double x, double y, double z)
        {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Builder setRotation(Vec3 rotation)
        {
            this.rotation = rotation;
            return this;
        }

        public Builder setRotation(double x, double y, double z)
        {
            this.rotation = new Vec3(x, y, z);
            return this;
        }

        public Builder setScale(float scale)
        {
            this.scale = scale;
            return this;
        }

        public Builder addModelLocation(ResourceLocation location)
        {
            this.models.add(location);
            return this;
        }

        public Builder addModelLocation(ResourceLocation location, ResourceLocation ... disabledCosmetics)
        {
            this.models.add(location);

            this.disabledCosmetics
                    .computeIfAbsent(location, l -> new ArrayList<>())
                    .addAll(Arrays.asList(disabledCosmetics));

            return this;
        }

        public Builder addAction(Action action)
        {
            this.actions.add(() -> action);
            return this;
        }

        public CosmeticProperties build()
        {
            CosmeticProperties properties = new CosmeticProperties(
                    this.id,
                    this.offset,
                    this.rotation,
                    this.scale,
                    this.actions
            );

            properties.setModelLocations(this.models);
            properties.setDisabledCosmetics(this.disabledCosmetics);

            return properties;
        }
    }
}