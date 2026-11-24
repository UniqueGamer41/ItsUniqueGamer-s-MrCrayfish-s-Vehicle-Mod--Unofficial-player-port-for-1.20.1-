package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.AtvEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.model.PlayerModel;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class AtvRenderer extends AbstractLandVehicleRenderer<AtvEntity>
{
    public AtvRenderer(EntityType<AtvEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable AtvEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        //Body
        this.renderDamagedPart(vehicle, VehicleModels.ATV_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        //Handle bar transformations
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.3375, 0.25);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-45F));
        matrixStack.translate(0.0, -0.025, 0);

//        if(vehicle != null)
//        {
//            float wheelAngle = vehicle.prevWheelAngle + (vehicle.wheelAngle - vehicle.prevWheelAngle) * partialTicks;
//            float wheelAngleNormal = wheelAngle / 45F;
//            float turnRotation = wheelAngleNormal * 15F;
//            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(turnRotation));
//        }

        this.renderSteeringWheel(vehicle, VehicleModels.ATV_HANDLES, 0.0, 0.0, -0.3375, 0.7F, -15F, matrixStack, renderTypeBuffer, light, partialTicks);

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(AtvEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 12F;
        model.rightArm.xRot = (float) Math.toRadians(-65F - turnRotation);
        model.rightArm.yRot = (float) Math.toRadians(15F);
        model.leftArm.xRot = (float) Math.toRadians(-65F + turnRotation);
        model.leftArm.yRot = (float) Math.toRadians(-15F);

        if(entity.getControllingPassenger() != player)
        {
            model.rightArm.xRot = (float) Math.toRadians(-20F);
            model.rightArm.yRot = (float) Math.toRadians(0F);
            model.rightArm.zRot = (float) Math.toRadians(15F);
            model.leftArm.xRot = (float) Math.toRadians(-20F);
            model.leftArm.yRot = (float) Math.toRadians(0F);
            model.leftArm.zRot = (float) Math.toRadians(-15F);
            model.rightLeg.xRot = (float) Math.toRadians(-85F);
            model.rightLeg.yRot = (float) Math.toRadians(30F);
            model.leftLeg.xRot = (float) Math.toRadians(-85F);
            model.leftLeg.yRot = (float) Math.toRadians(-30F);
            return;
        }

        model.rightLeg.xRot = (float) Math.toRadians(-65F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-65F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.ATV_BODY, parts, transforms,
                    MatrixTransform.scale(1.25F));

            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_ATV, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.ATV_HANDLES, parts, transforms,
                    MatrixTransform.translate(0.0F, 0.3375F, 0.25F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-45F)),
                    MatrixTransform.translate(0.0F, -0.025F, 0.0F));
//            TransformHelper.createTransformListForPart(VehicleModels.TOW_BAR, parts, transforms,
//                    MatrixTransform.rotate(Axis.POSITIVE_Y.rotationDegrees(180F)),
//                    MatrixTransform.translate(0.0F, 0.5F, 1.05F));

            TransformHelper.createFuelFillerTransforms(ModEntities.ATV.get(), VehicleModels.ATV_FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createTowBarTransforms(ModEntities.ATV.get(), VehicleModels.TOW_BAR, parts);
            //EntityRayTracer.createFuelPartTransforms(ModEntities.ATV.get(), SpecialModels.SMALL_FUEL_DOOR_CLOSED, parts, transforms);
            //TransformHelper.createKeyPortTransforms(ModEntities.ATV.get(), parts, transforms);
        };
    }

    @Override
    protected boolean shouldRenderFuelLid()
    {
        return false;
    }
}