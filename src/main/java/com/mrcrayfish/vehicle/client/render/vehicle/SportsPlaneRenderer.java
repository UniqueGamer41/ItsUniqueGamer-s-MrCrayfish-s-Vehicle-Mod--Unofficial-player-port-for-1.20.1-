package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractPlaneRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.SportsPlaneEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class SportsPlaneRenderer extends AbstractPlaneRenderer<SportsPlaneEntity>
{
    public SportsPlaneRenderer(EntityType<SportsPlaneEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable SportsPlaneEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_LEG, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_WING, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_WINGS, matrixStack, renderTypeBuffer, light, partialTicks);
       // this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_PROPELLER, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_WHEEL_COVER, matrixStack,renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_RIGHT_AILERON, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_LEFT_AILERON, matrixStack, renderTypeBuffer, light, partialTicks);
        //this.renderDamagedPart(vehicle, VehicleModels.SPORTS_PLANE_ELEVATOR, matrixStack, renderTypeBuffer, light, partialTicks);





    }

    @Override
    public void applyPlayerModel(SportsPlaneEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        model.rightLeg.xRot = (float) Math.toRadians(-85F);
        model.rightLeg.yRot = (float) Math.toRadians(10F);
        model.leftLeg.xRot = (float) Math.toRadians(-85F);
        model.leftLeg.yRot = (float) Math.toRadians(-10F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_BODY, parts, transforms);
            //TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WINGS, parts, transforms);
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WING, parts, transforms,
//                    MatrixTransform.translate(0, -0.1875F, 0.5F),
//                    MatrixTransform.rotate(Axis.POSITIVE_Z.rotation(180F)),
//                    MatrixTransform.translate(0.875F, 0.0625F, 0.0F),
//                    MatrixTransform.rotate(Axis.POSITIVE_X.rotation(5F)));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WING, parts, transforms,
//                    MatrixTransform.translate(0.875F, -0.1875F, 0.5F),
//                    MatrixTransform.rotate(Axis.POSITIVE_X.rotation(-5F)),
//                    MatrixTransform.translate(0.0F, -0.5F, 0.0F),
//                    MatrixTransform.scale(0.85F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WHEEL_COVER, parts, transforms,
//                    MatrixTransform.translate(0.0F, -0.1875F, 1.5F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_LEG, parts, transforms,
//                    MatrixTransform.translate(0.0F, -0.1875F, 1.5F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WHEEL_COVER, parts, transforms,
//                    MatrixTransform.translate(-0.46875F, -0.1875F, 0.125F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_LEG, parts, transforms,
//                    MatrixTransform.translate(-0.46875F, -0.1875F, 0.125F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_WHEEL_COVER, parts, transforms,
//                    MatrixTransform.translate(-0.46875F, -0.1875F, 0.125F));
//            TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_LEG, parts, transforms,
//                    MatrixTransform.translate(0.46875F, -0.1875F, 0.125F));
              TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_PROPELLER, parts, transforms);
              TransformHelper.createFuelFillerTransforms(ModEntities.SPORTS_PLANE.get(), VehicleModels.SPORTS_PLANE_FUEL_DOOR_CLOSED, parts, transforms);
              TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_KEYHOLE,  parts, transforms);
            //TransformHelper.createTransformListForPart(VehicleModels.SPORTS_PLANE_ELEVATOR, parts, transforms);

        };
    }
}
