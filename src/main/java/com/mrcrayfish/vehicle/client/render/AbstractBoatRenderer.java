package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.BoatEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractBoatRenderer<T extends BoatEntity> extends AbstractPoweredRenderer<T>
{
    public AbstractBoatRenderer(EntityType<T> type, Supplier<VehicleProperties> defaultProperties)
    {
        super(type, defaultProperties);
    }

    @Override
    public void setupTransformsAndRender(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        matrixStack.pushPose();

        VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
        Transform bodyPosition = properties.getBodyTransform();
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) bodyPosition.getRotX()));
        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) bodyPosition.getRotY()));
        matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) bodyPosition.getRotZ()));

        //TODO add back boat rotation
        float steering = vehicle.getSteeringAngle();

        double speed = vehicle.motion.horizontalDistance();
        double maxSpeedPerTick = Math.max(0.01D, vehicle.getMaxSpeed() / 20.0D);

        float speedFactor = (float) Math.min(
                1.0D,
                speed / (maxSpeedPerTick * 0.6D)
        );

        float leanAngle =
                (steering / 65.0F)
                        * 15.0F
                        * speedFactor;

        matrixStack.mulPose(
                Axis.POSITIVE_Z.rotationDegrees(-leanAngle)
        );

        //this.renderRotationLine(matrixStack, 0xFF0000);

        //Translate the body
        matrixStack.translate(bodyPosition.getX(), bodyPosition.getY(), bodyPosition.getZ());

        //Translate the vehicle to match how it is shown in the model creator
        matrixStack.translate(0.0, 0.5, 0.0);

        //Apply vehicle scale
        matrixStack.translate(0.0, -0.5, 0.0);
        matrixStack.scale((float) bodyPosition.getScale(), (float) bodyPosition.getScale(), (float) bodyPosition.getScale());
        matrixStack.translate(0.0, 0.5, 0.0);

        //Translate the vehicle so it's axles are half way into the ground
        matrixStack.translate(0.0, properties.getAxleOffset() * 0.0625, 0.0);

        //Translate the vehicle so it's actually riding on it's wheels
        matrixStack.translate(0.0, properties.getWheelOffset() * 0.0625, 0.0);

        //Render body
        this.render(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);

        this.renderEngine(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
        this.renderFuelFiller(vehicle, matrixStack, renderTypeBuffer, light);
        this.renderIgnition(vehicle, matrixStack, renderTypeBuffer, light);

        matrixStack.popPose();
    }
}
