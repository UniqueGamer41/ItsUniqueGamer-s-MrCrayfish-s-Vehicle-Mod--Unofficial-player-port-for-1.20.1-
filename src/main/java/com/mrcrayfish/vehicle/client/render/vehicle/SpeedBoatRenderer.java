package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractBoatRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.JetSkiEntity;
import com.mrcrayfish.vehicle.entity.vehicle.SpeedBoatEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SpeedBoatRenderer extends AbstractBoatRenderer<SpeedBoatEntity>
{
    public SpeedBoatRenderer(EntityType<SpeedBoatEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable SpeedBoatEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        //Render the body
        this.renderDamagedPart(vehicle, VehicleModels.SPEED_BOAT_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        this.renderSteeringWheel(vehicle, VehicleModels.GO_KART_STEERING_WHEEL, 0.0, 2.76, -1.0, 1.0F, -45.0F, matrixStack, renderTypeBuffer, light, partialTicks);

        //Render the handles bars
        matrixStack.pushPose();

        matrixStack.translate(0, 0.215, -0.125);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-45F));

        float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 15F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(steeringWheelRotation));



        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(SpeedBoatEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 15F / 2F;
        model.rightArm.xRot = (float) Math.toRadians(-65F - steeringWheelRotation);
        model.rightArm.yRot = (float) Math.toRadians(15F);
        //model.bipedRightArm.offsetZ = -0.1F * wheelAngleNormal; //TODO test this out
        model.leftArm.xRot = (float) Math.toRadians(-65F + steeringWheelRotation);
        model.leftArm.yRot = (float) Math.toRadians(-15F);
        //model.bipedLeftArm.offsetZ = 0.1F * wheelAngleNormal;

        if(entity.getControllingPassenger() != player)
        {
            model.rightArm.xRot = (float) Math.toRadians(-55F);
            model.rightArm.yRot = (float) Math.toRadians(0F);
            model.leftArm.xRot = (float) Math.toRadians(-55F);
            model.leftArm.yRot = (float) Math.toRadians(0F);
        }

        model.rightLeg.xRot = (float) Math.toRadians(-65F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-65F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Override
    protected boolean shouldRenderFuelLid()
    {
        return false;
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (entityRayTracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.SPEED_BOAT_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    MatrixTransform.translate(0.0F, 0.215F, -3.125F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-45F)),
                    MatrixTransform.translate(0.0F, 0.02F, 0.0F));
            TransformHelper.createFuelFillerTransforms(ModEntities.SPEED_BOAT.get(), VehicleModels.SPEED_BOAT_FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_SB, parts, transforms);

        };
    }
}
