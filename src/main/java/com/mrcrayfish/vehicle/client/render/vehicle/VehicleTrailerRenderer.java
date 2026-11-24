package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractTrailerRenderer;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.trailer.VehicleTrailerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class VehicleTrailerRenderer extends AbstractTrailerRenderer<VehicleTrailerEntity>
{
    public VehicleTrailerRenderer(EntityType<VehicleTrailerEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable VehicleTrailerEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.VEHICLE_TRAILER, matrixStack, renderTypeBuffer, light, partialTicks);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.VEHICLE_TRAILER, parts, transforms);
        };
    }
}
