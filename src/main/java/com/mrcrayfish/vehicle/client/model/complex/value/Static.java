package com.mrcrayfish.vehicle.client.model.complex.value;

import com.google.gson.*;
import com.mrcrayfish.vehicle.entity.VehicleEntity;

/**
 * Author: MrCrayfish
 */
public record Static(double value) implements IValue
{
    public static final Static ZERO = new Static(0.0);

    public static JsonDeserializer<Static> deserializer()
    {
        return (json, type, ctx) -> fromJson(json);
    }

    public static Static fromJson(JsonElement json)
    {
        if(json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
        {
            return new Static(json.getAsDouble());
        }
        throw new JsonParseException("Static values must be a number");
    }

    public double getValue(VehicleEntity entity, float partialTicks)
    {
        return this.value;
    }
}
