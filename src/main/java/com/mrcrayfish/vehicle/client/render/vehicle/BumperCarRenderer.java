package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.BumperCarEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class BumperCarRenderer extends AbstractLandVehicleRenderer<BumperCarEntity>
{
    public BumperCarRenderer(EntityType<BumperCarEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable BumperCarEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.BUMPER_CAR_BODY, matrixStack, renderTypeBuffer, light, partialTicks);


        this.renderSteeringWheel(vehicle, VehicleModels.GO_KART_STEERING_WHEEL, -0.0, 2.50, -0.3375, 0.7F, -40F, matrixStack, renderTypeBuffer, light, partialTicks);
        VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);

    }

    @Override
    public void applyPlayerModel(BumperCarEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        if(entity.getControllingPassenger() == player)
        {
            model.rightLeg.xRot = (float) Math.toRadians(-85F);
            model.rightLeg.yRot = (float) Math.toRadians(10F);
            model.leftLeg.xRot = (float) Math.toRadians(-85F);
            model.leftLeg.yRot = (float) Math.toRadians(-10F);
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
            float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 45F / 6F;
            model.rightArm.xRot = (float) Math.toRadians(-65F - steeringWheelRotation);
            model.rightArm.yRot = (float) Math.toRadians(-7F);
            model.leftArm.xRot = (float) Math.toRadians(-65F + steeringWheelRotation);
            model.leftArm.yRot = (float) Math.toRadians(7F);
        }
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (entityRayTracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.BUMPER_CAR_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.KEY_HOLE_BC, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    MatrixTransform.translate(-1.6569F, -4.0F, -0.5F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(0F)),
                    MatrixTransform.rotate(Axis.POSITIVE_Y.rotationDegrees(0F)),
                    MatrixTransform.rotate(Axis.POSITIVE_Z.rotationDegrees(0F)),
                    MatrixTransform.translate(1.6569F, 4.0F, 0.5F),
                    MatrixTransform.scale(0.7F));
            //TransformHelper.createFuelFillerTransforms(ModEntities.BUMPER_CAR.get(), VehicleModels.FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createTransformListForPart(
                    VehicleModels.BUMPER_CAR_FUEL_DOOR_CLOSED,
                    parts,
                    transforms,
                    RayTraceFunction.FUNCTION_FUELING,
                    MatrixTransform.translate(-8.0F * 0.0625F, 22.5F * 0.0625F, -8.0F * 0.0625F),
                    MatrixTransform.rotate(Axis.NEGATIVE_X.rotationDegrees(90F))
            );

        };


    }

    @Override
    protected boolean shouldRenderFuelLid()
    {
        return false;
    }
}
