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
import com.mrcrayfish.vehicle.util.port.Quaternion;
import com.mrcrayfish.vehicle.util.port.Vector3f;

/**
 * Author: MrCrayfish
 */
public record Rotate(IValue x, IValue y, IValue z) implements Transform
{
    public static JsonDeserializer<Rotate> deserializer()
    {
        return (json, type, ctx) -> fromJson(json.getAsJsonObject(), ctx);
    }

    public static Rotate fromJson(JsonObject object, JsonDeserializationContext ctx)
    {
        IValue x = get(object, "x", ctx);
        IValue y = get(object, "y", ctx);
        IValue z = get(object, "z", ctx);

        return new Rotate(x, y, z);
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

        throw new JsonParseException("Rotate values can only be a number or object");
    }

    @Override
    public void apply(VehicleEntity entity, PoseStack stack, float partialTicks)
    {
        stack.mulPose(Vector3f.XP.rotationDegrees((float) this.x.getValue(entity, partialTicks)));
        stack.mulPose(Vector3f.YP.rotationDegrees((float) this.y.getValue(entity, partialTicks)));
        stack.mulPose(Vector3f.ZP.rotationDegrees((float) this.z.getValue(entity, partialTicks)));
    }

    @Override
    public MatrixTransform create(VehicleEntity entity, float partialTicks)
    {
        return MatrixTransform.rotate(
                new Quaternion(
                        (float) this.x.getValue(entity, partialTicks),
                        (float) this.y.getValue(entity, partialTicks),
                        (float) this.z.getValue(entity, partialTicks),
                        true
                )
        );
    }
}
