package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractMotorcycleRenderer;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.DirtBikeEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class DirtBikeRenderer extends AbstractMotorcycleRenderer<DirtBikeEntity>
{
    public DirtBikeRenderer(EntityType<DirtBikeEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable DirtBikeEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        // Render the main body of the dirt bike
        this.renderDamagedPart(vehicle, VehicleModels.DIRT_BIKE_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
        this.renderSteeringWheel(vehicle, VehicleModels.DIRT_BIKE_HANDLES, 0.0, 0.0, 0.0, 0.7F, 0.0F, matrixStack, renderTypeBuffer, light, partialTicks);

        // If you uncomment the below code, add push/popPose back as needed
        /*
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.0, 10.5 * 0.0625);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-22.5F));

        if (vehicle != null)
        {
            float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
            float turnRotation = (wheelAngle / 45F) * 25F;
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(turnRotation));
        }

        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(22.5F));
        matrixStack.translate(0.0, 0.0, -10.5 * 0.0625);

        this.renderDamagedPart(vehicle, VehicleModels.DIRT_BIKE_HANDLES, matrixStack, renderTypeBuffer, light, partialTicks);

        // If the front wheel exists, render it
        ItemStack wheelStack = this.wheelStackProperty.get(vehicle);
        if (!wheelStack.isEmpty())
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            WheelType wheel = properties.getWheels().stream().filter(w -> w.getPosition() == WheelType.Position.FRONT).findFirst().orElse(null);

            if (wheel != null)
            {
                matrixStack.pushPose();
                matrixStack.translate(0, -0.5, 0);
                matrixStack.translate(properties.getFirstFrontWheel().getOffsetX() * 0.0625, properties.getAxleOffset() * 0.0625, wheel.getOffsetZ() * 0.0625);

                if (vehicle != null)
                {
                    float frontWheelSpin = Mth.lerp(partialTicks, vehicle.prevFrontWheelRotation, vehicle.frontWheelRotation);
                    if (vehicle.isMoving())
                    {
                        matrixStack.mulPose(Axis.XP.rotationDegrees(-frontWheelSpin));
                    }
                }

                matrixStack.scale(wheel.getScaleX(), wheel.getScaleY(), wheel.getScaleZ());
                matrixStack.mulPose(Axis.YP.rotationDegrees(180F));
                RenderUtil.renderColoredModel(RenderUtil.getModel(wheelStack), ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();
            }
        }
        matrixStack.popPose();
        */
    }

    @Override
    public void applyPlayerModel(DirtBikeEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
        if (index == 0) // Driver seat
        {
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float wheelAngleNormal = wheelAngle / 45F;
            float turnRotation = wheelAngleNormal * 8F;
            model.rightArm.xRot = (float) Math.toRadians(-55F - turnRotation);
            model.leftArm.xRot = (float) Math.toRadians(-55F + turnRotation);
        }
        else if (index == 1) // Passenger seat
        {
            model.rightArm.xRot = (float) Math.toRadians(-45F);
            model.rightArm.zRot = (float) Math.toRadians(-10F);
            model.leftArm.xRot = (float) Math.toRadians(-45F);
            model.leftArm.zRot = (float) Math.toRadians(10F);
        }

        model.rightLeg.xRot = (float) Math.toRadians(-45F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-45F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Override
    public void applyPlayerRender(DirtBikeEntity entity, Player player, float partialTicks, PoseStack matrixStack, VertexConsumer builder)
    {
        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
        if (index != -1)
        {
            VehicleProperties properties = entity.getProperties();
            Seat seat = properties.getSeats().get(index);
            Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), properties.getBodyTransform().getScale()).scale(0.0625);
            double scale = 32.0 / 30.0;
            double offsetX = seatVec.x * scale;
            double offsetY = (seatVec.y + player.getMyRidingOffset()) * scale + 24 * 0.0625;
            double offsetZ = -seatVec.z * scale;

            // Apply rotation during player render
            /*
            float currentSpeedNormal = (entity.prevCurrentSpeed + (entity.currentSpeed - entity.prevCurrentSpeed) * partialTicks) / entity.getMaxSpeed();
            float turnAngleNormal = (entity.prevTurnAngle + (entity.turnAngle - entity.prevTurnAngle) * partialTicks) / 45F;
            matrixStack.mulPose(Axis.ZP.rotationDegrees(turnAngleNormal * currentSpeedNormal * 20F));
            */
        }
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (entityRayTracer, transforms, parts) -> {
            TransformHelper.createTransformListForPart(VehicleModels.KEYHOLE_DBI, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.DIRT_BIKE_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.DIRT_BIKE_HANDLES, parts, transforms);
            TransformHelper.createFuelFillerTransforms(ModEntities.DIRT_BIKE.get(), VehicleModels.DIRT_BIKE_FUEL_DOOR_CLOSED, parts, transforms);
        };
    }
}
