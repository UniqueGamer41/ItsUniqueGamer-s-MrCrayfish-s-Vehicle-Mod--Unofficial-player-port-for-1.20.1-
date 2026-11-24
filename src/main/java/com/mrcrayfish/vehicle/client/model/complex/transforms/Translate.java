package com.mrcrayfish.vehicle.client.model.complex.transforms;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.model.complex.value.Dynamic;
import com.mrcrayfish.vehicle.client.model.complex.value.IValue;
import com.mrcrayfish.vehicle.client.model.complex.value.Static;
import com.mrcrayfish.vehicle.entity.VehicleEntity;

/**
 * Author: MrCrayfish
 */
public record Translate(IValue x, IValue y, IValue z) implements Transform
{
    private static final float EPSILON = 0.0625F;

    public static JsonDeserializer<Translate> deserializer()
    {
        return (json, type, ctx) -> fromJson(json.getAsJsonObject(), ctx);
    }

    public static Translate fromJson(JsonObject object, JsonDeserializationContext ctx)
    {
        IValue x = get(object, "x", ctx);
        IValue y = get(object, "y", ctx);
        IValue z = get(object, "z", ctx);

        return new Translate(x, y, z);
    }

    private static IValue get(JsonObject object, String key, JsonDeserializationContext ctx)
    {
        if(!object.has(key))
        {
            return Static.ZERO;
        }

        JsonElement e = object.get(key);
        if(e.isJsonObject())
        {
            return ctx.deserialize(e, Dynamic.class);
        }
        else if(e.isJsonPrimitive())
        {
            return ctx.deserialize(e, Static.class);
        }

        throw new JsonParseException("Translate values can only be a number or object");
    }

    @Override
    public void apply(VehicleEntity entity, PoseStack stack, float partialTicks)
    {
        stack.translate(
                this.x.getValue(entity, partialTicks) * EPSILON,
                this.y.getValue(entity, partialTicks) * EPSILON,
                this.z.getValue(entity, partialTicks) * EPSILON
        );
    }

    @Override
    public MatrixTransform create(VehicleEntity entity, float partialTicks)
    {
        return MatrixTransform.translate(
                (float) this.x.getValue(entity, partialTicks) * EPSILON,
                (float) this.y.getValue(entity, partialTicks) * EPSILON,
                (float) this.z.getValue(entity, partialTicks) * EPSILON
        );
    }
}
