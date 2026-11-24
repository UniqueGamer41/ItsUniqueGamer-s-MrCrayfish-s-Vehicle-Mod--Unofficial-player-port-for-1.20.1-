package com.mrcrayfish.vehicle.client.model.complex.value;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mrcrayfish.vehicle.client.model.complex.ComplexModelDynamicSourceRegistry;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public record Dynamic(BiFunction<VehicleEntity, Float, Double> source, boolean inverse, float scale) implements IValue
{
    public static JsonDeserializer<Dynamic> deserializer()
    {
        return (json, type, ctx) -> fromJson(json);
    }

    public static Dynamic fromJson(JsonElement json)
    {
        if(json.isJsonObject())
        {
            JsonObject object = json.getAsJsonObject();
            BiFunction<VehicleEntity, Float, Double> source = ComplexModelDynamicSourceRegistry.getSource(new ResourceLocation(GsonHelper.getAsString(object, "source"))).orElseThrow(RuntimeException::new);
            if(source == null) throw new JsonParseException("Invalid source: " + GsonHelper.getAsString(object, "source"));
            boolean inverse = GsonHelper.getAsBoolean(object, "inverse", false);
            float scale = GsonHelper.getAsFloat(object, "scale", 1.0F);
            return new Dynamic(source, inverse, scale);
        }

        throw new JsonParseException("Dynamic values must be object");
    }

    public double getValue(VehicleEntity entity, float partialTicks)
    {
        double value = this.source.apply(entity, partialTicks) * this.scale;

        return this.inverse ? -value : value;
    }
}
