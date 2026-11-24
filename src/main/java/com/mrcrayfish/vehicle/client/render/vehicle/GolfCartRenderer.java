package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractHelicopterRenderer;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.GolfCartEntity;
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
public class GolfCartRenderer extends AbstractLandVehicleRenderer<GolfCartEntity>
{
    public GolfCartRenderer(EntityType<GolfCartEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable GolfCartEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        //Render the body
        this.renderDamagedPart(vehicle, VehicleModels.GOLF_CART_BODY, matrixStack, renderTypeBuffer, light, partialTicks);

        //Render the handles bars
        matrixStack.pushPose();

        // Positions the steering wheel in the correct position
        matrixStack.translate(-0.345, 0.420, 0.1);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-45F));
        matrixStack.translate(0, -0.02, 0);
        matrixStack.scale(0.95F, 0.95F, 0.95F);

        // Rotates the steering wheel based on the wheel angle
        float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
        float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
        float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(steeringWheelRotation));

        this.renderDamagedPart(vehicle, VehicleModels.GO_KART_STEERING_WHEEL, matrixStack, renderTypeBuffer, light, partialTicks);

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(GolfCartEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        model.rightLeg.xRot = (float) Math.toRadians(-80F);
        model.rightLeg.yRot = (float) Math.toRadians(15F);
        model.leftLeg.xRot = (float) Math.toRadians(-80F);
        model.leftLeg.yRot = (float) Math.toRadians(-15F);

        if(entity.getControllingPassenger() == player)
        {
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
            float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F / 2F;
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
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.GOLF_CART_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    MatrixTransform.translate(-0.345F, 0.425F, 0.1F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-45F)),
                    MatrixTransform.translate(0.0F, -0.02F, 0.0F),
                    MatrixTransform.scale(0.95F));
            TransformHelper.createFuelFillerTransforms(ModEntities.GOLF_CART.get(), VehicleModels.GOLF_CART_FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createIgnitionTransforms(ModEntities.GOLF_CART.get(), parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GOLF_KH, parts, transforms);
        };
    }
}
