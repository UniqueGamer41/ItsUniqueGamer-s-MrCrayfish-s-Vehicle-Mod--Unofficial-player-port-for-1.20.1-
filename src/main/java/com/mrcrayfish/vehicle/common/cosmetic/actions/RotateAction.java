package com.mrcrayfish.vehicle.common.cosmetic.actions;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.common.cosmetic.actions.source.RotateActionSource;
import com.mrcrayfish.vehicle.common.cosmetic.actions.source.RotateActionSourceRegistry;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.util.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class RotateAction extends Action
{
    private final RotateActionSource source;
    private final Axis axis;
    private final float scale;

    public RotateAction(RotateActionSource source, Axis axis, float scale)
    {
        this.source = source;
        this.axis = axis;
        this.scale = scale;
    }

    @Override
    public void beforeRender(PoseStack stack, VehicleEntity vehicle, float partialTicks)
    {
        stack.mulPose(this.axis.getAxis().rotationDegrees(this.source.valueFunction().apply(vehicle, partialTicks)));
    }

    @Override
    public void serialize(JsonObject object)
    {
        object.addProperty("source", this.source.key().toString());
        object.addProperty("axis", this.axis.getKey());
        object.addProperty("scale", this.scale);
    }

    public static Supplier<RotateAction> createSupplier(JsonObject object)
    {
        RotateActionSource source = RotateActionSourceRegistry.getSource(new ResourceLocation(GsonHelper.getAsString(object, "source"))).orElseThrow(RuntimeException::new);

        Axis axis = Axis.fromKey(GsonHelper.getAsString(object, "axis"));
        float scale = GsonHelper.getAsFloat(object, "scale", 1.0F);
        return () -> new RotateAction(source, axis, scale);
    }
}
