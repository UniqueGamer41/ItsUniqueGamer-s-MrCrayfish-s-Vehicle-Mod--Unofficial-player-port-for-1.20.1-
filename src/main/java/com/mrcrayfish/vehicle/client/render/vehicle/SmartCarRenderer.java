package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.MiniBusEntity;
import com.mrcrayfish.vehicle.entity.vehicle.SmartCarEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SmartCarRenderer extends AbstractLandVehicleRenderer<SmartCarEntity>
{
    public SmartCarRenderer(EntityType<SmartCarEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable SmartCarEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.SMART_CAR_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
        this.renderSteeringWheel(vehicle, VehicleModels.SMART_CAR_STEERING_WHEEL,
                0.0, 3.2, 5.8,
                0.9F,
                -67.5F,
                matrixStack, renderTypeBuffer, light, partialTicks);
    }

    @Override
    public void applyPlayerModel(SmartCarEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        if(entity.getControllingPassenger() == player)
        {
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float turnRotation = (wheelAngle / 45F) * 6F;
            model.rightArm.xRot = (float) Math.toRadians(-80F - turnRotation);
            model.rightArm.yRot = (float) Math.toRadians(-7F);
            model.leftArm.xRot = (float) Math.toRadians(-80F + turnRotation);
            model.leftArm.yRot = (float) Math.toRadians(7F);
        }
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.SMART_CAR_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    MatrixTransform.translate(0.0F, 0.2F, 0.3F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-67.5F)),
                    MatrixTransform.translate(0.0F, -0.02F, 0.0F),
                    MatrixTransform.scale(0.9F));
            TransformHelper.createTowBarTransforms(ModEntities.SMART_CAR.get(), VehicleModels.TOW_BAR, parts);
            TransformHelper.createFuelFillerTransforms(ModEntities.SMART_CAR.get(), VehicleModels.SMART_CAR_FUEL_DOOR_CLOSED, parts, transforms);
            //TransformHelper.createIgnitionTransforms(ModEntities.SMART_CAR.get(), parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_SC, parts, transforms);
        };
    }

    @Override
    public ComponentModel getTowBarModel()
    {
        return VehicleModels.BIG_TOW_BAR;
    }
}
