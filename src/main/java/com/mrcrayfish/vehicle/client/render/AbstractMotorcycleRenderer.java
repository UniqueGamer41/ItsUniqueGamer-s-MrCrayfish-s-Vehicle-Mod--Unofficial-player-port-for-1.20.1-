package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.MotorcycleEntity;
import com.mrcrayfish.vehicle.entity.properties.LandProperties;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractMotorcycleRenderer<T extends MotorcycleEntity> extends AbstractLandVehicleRenderer<T>
{
    public AbstractMotorcycleRenderer(EntityType<T> type, Supplier<VehicleProperties> defaultProperties)
    {
        super(type, defaultProperties);
    }

    @Override
    public void setupTransformsAndRender(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.bodyRollProperty.get(vehicle, partialTicks)));

        VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
        Transform bodyPosition = properties.getBodyTransform();
        matrixStack.scale((float) bodyPosition.getScale(), (float) bodyPosition.getScale(), (float) bodyPosition.getScale());
        matrixStack.translate(bodyPosition.getX() * 0.0625, bodyPosition.getY() * 0.0625, bodyPosition.getZ() * 0.0625);

        if(properties.canTowTrailers())
        {
            matrixStack.pushPose();
            double inverseScale = 1.0 / bodyPosition.getScale();
            matrixStack.scale((float) inverseScale, (float) inverseScale, (float) inverseScale);
            Vec3 towBarOffset = properties.getTowBarOffset().scale(bodyPosition.getScale());
            matrixStack.translate(towBarOffset.x * 0.0625, towBarOffset.y * 0.0625 + 0.5, towBarOffset.z * 0.0625);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            RenderObjectHelper.renderColoredModel(this.getTowBarModel().getBaseModel(), ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }

        // Fixes the origin
        matrixStack.translate(0.0, 0.5, 0.0);

        // Translate the vehicle so the center of the axles are touching the ground
        matrixStack.translate(0.0, properties.getAxleOffset() * 0.0625, 0.0);

        // Translate the vehicle so it's actually riding on it's wheels
        matrixStack.translate(0.0, properties.getWheelOffset() * 0.0625, 0.0);

        /* Rotates the wheel based relative to the rear axel to create a wheelie */
        if(properties.getExtended(LandProperties.class).canWheelie())
        {
            Vec3 rearAxleOffset = properties.getExtended(PoweredProperties.class).getRearAxleOffset();
            matrixStack.translate(0.0, -0.5, 0.0);
            matrixStack.translate(0.0, -properties.getAxleOffset() * 0.0625, 0.0);
            matrixStack.translate(0.0, 0.0, rearAxleOffset.z * 0.0625);
            float p = this.wheelieProgressProperty.get(vehicle, partialTicks);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-30F * this.boostStrengthProperty.get(vehicle) * p));
            matrixStack.translate(0.0, 0.0, -rearAxleOffset.z * 0.0625);
            matrixStack.translate(0.0, properties.getAxleOffset() * 0.0625, 0.0);
            matrixStack.translate(0.0, 0.5, 0.0);
        }

        //Render body
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) bodyPosition.getRotX()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) bodyPosition.getRotY()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) bodyPosition.getRotZ()));
        this.render(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
        matrixStack.popPose();

        this.renderWheels(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
        this.renderEngine(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
        this.renderFuelFiller(vehicle, matrixStack, renderTypeBuffer, light);
        this.renderIgnition(vehicle, matrixStack, renderTypeBuffer, light);
        this.renderCosmetics(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);

        matrixStack.popPose();
    }
}
