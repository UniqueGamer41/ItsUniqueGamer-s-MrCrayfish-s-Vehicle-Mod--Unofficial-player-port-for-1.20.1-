package com.mrcrayfish.vehicle.client.model.complex.transforms;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.entity.VehicleEntity;

/**
 * Author: MrCrayfish
 */
public interface Transform
{
    void apply(VehicleEntity entity, PoseStack stack, float partialTicks);

    MatrixTransform create(VehicleEntity entity, float partialTicks);
}
