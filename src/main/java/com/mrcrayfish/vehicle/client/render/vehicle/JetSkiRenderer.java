package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractBoatRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.JetSkiEntity;
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
public class JetSkiRenderer extends AbstractBoatRenderer<JetSkiEntity>
{
    public JetSkiRenderer(EntityType<JetSkiEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable JetSkiEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        //Render the body
        this.renderDamagedPart(vehicle, VehicleModels.JET_SKI_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        //Render the handles bars
        matrixStack.pushPose();

        matrixStack.translate(0, 0.355, 0.225);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-45F));

        float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 15F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(steeringWheelRotation));

        this.renderDamagedPart(vehicle, VehicleModels.QUAD_BIKE_HANDLES, matrixStack, renderTypeBuffer, light, partialTicks);

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(JetSkiEntity entity, Player player, PlayerModel<?> model, float partialTicks)
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
            TransformHelper.createTransformListForPart(VehicleModels.JET_SKI_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.QUAD_BIKE_HANDLES, parts, transforms,
                    MatrixTransform.translate(0.0F, 0.375F, 0.25F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-45F)),
                    MatrixTransform.translate(0.0F, 0.02F, 0.0F));
            TransformHelper.createFuelFillerTransforms(ModEntities.JET_SKI.get(), VehicleModels.JET_SKI_FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_JK, parts, transforms);
        };
    }
}
