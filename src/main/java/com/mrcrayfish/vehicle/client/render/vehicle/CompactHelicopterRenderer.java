package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractHelicopterRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.CompactHelicopterEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class CompactHelicopterRenderer extends AbstractHelicopterRenderer<CompactHelicopterEntity>
{
    public CompactHelicopterRenderer(EntityType<CompactHelicopterEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable CompactHelicopterEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.HELICOPTER_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.HELICOPTER_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_CH, parts, transforms);
            TransformHelper.createTransformListForPart(
                    VehicleModels.HELI_FD,
                    parts,
                    transforms,
                    RayTraceFunction.FUNCTION_FUELING,
                    MatrixTransform.translate(-8.0F * 0.0625F, 22.5F * 0.0625F, -8.0F * 0.0625F),
                    MatrixTransform.rotate(Axis.NEGATIVE_X.rotationDegrees(90F))
            );
        };
    }
}
