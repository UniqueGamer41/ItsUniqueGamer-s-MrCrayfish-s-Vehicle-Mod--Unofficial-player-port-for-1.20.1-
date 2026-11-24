package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
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
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class MiniBusRenderer extends AbstractLandVehicleRenderer<MiniBusEntity>
{
    public MiniBusRenderer(EntityType<MiniBusEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable MiniBusEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_ROOF, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_ROOF_RACKS, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_FRONT_ROOF, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_DASHBOARD, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_LEFT_DOOR, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_AIRCON_LADDER_REAR_DECOR, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_RIGHT_DOOR, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_SLIDING_DOOR, matrixStack, renderTypeBuffer, light, partialTicks);
//        this.renderDamagedPart(vehicle, VehicleModels.MINI_BUS_COSMETIC_STOCK_SEAT, matrixStack, renderTypeBuffer, light, partialTicks);
        this.renderSteeringWheel(vehicle, VehicleModels.MINI_BUS_STEERING_WHEEL, -5.0, 4.5, 15.8, 0.2F, -67.5F, matrixStack, renderTypeBuffer, light, partialTicks);
        this.renderFuelFiller(vehicle, matrixStack, renderTypeBuffer, light);
        //this.renderFuelFiller(vehicle, matrixStack, renderTypeBuffer, light);
    }

    @Override
    public void applyPlayerModel(MiniBusEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        if(entity.getControllingPassenger() == player)
        {
            float wheelAngle = this.wheelAngleProperty.get(entity, partialTicks);
            float maxSteeringAngle = this.vehiclePropertiesProperty.get(entity).getExtended(PoweredProperties.class).getMaxSteeringAngle();
            float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F / 2F;
            model.rightArm.xRot = (float) Math.toRadians(-75F - steeringWheelRotation);
            model.rightArm.yRot = (float) Math.toRadians(-7F);
            model.leftArm.xRot = (float) Math.toRadians(-75F + steeringWheelRotation);
            model.leftArm.yRot = (float) Math.toRadians(7F);
        }
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            transforms.add(MatrixTransform.translate(10.0F, 7.0F, 0.0F));
            transforms.add(MatrixTransform.scale(1.2F));

            System.out.println("GLOBAL TRANSFORMS: " + transforms);
            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_BODY, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_ROOF, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_ROOF_RACKS, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_FRONT_ROOF, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_DASHBOARD, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_LEFT_DOOR, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_AIRCON_LADDER_REAR_DECOR, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_RIGHT_DOOR, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_DASHBOARD, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_COSMETIC_STOCK_SEAT, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(VehicleModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    MatrixTransform.translate(-0.2825F, 0.225F, 1.0625F),
                    MatrixTransform.rotate(Axis.POSITIVE_X.rotationDegrees(-67.5F)),
                    MatrixTransform.translate(0.0F, -0.02F, 0.0F),
                    MatrixTransform.scale(0.75F));
            TransformHelper.createTowBarTransforms(ModEntities.MINI_BUS.get(), VehicleModels.BIG_TOW_BAR, parts);
            //TransformHelper.createFuelFillerTransforms(ModEntities.MINI_BUS.get(), VehicleModels.FUEL_PORT_BODY, parts, transforms);
            TransformHelper.createFuelFillerTransforms(ModEntities.MINI_BUS.get(), VehicleModels.MINI_BUS_FUEL_DOOR_CLOSED, parts, transforms);



            //System.out.println("FUEL FILLER TRANSFORM RAYTRACE REGISTERED" + transforms);
            TransformHelper.createTransformListForPart(VehicleModels.MINI_BUS_KEYHOLE, parts, transforms);
        };
    }

    @Override
    protected boolean shouldRenderFuelLid()
    {
        return false;
    }

    @Override
    public ComponentModel getTowBarModel()
    {
        return VehicleModels.BIG_TOW_BAR;
    }
}
