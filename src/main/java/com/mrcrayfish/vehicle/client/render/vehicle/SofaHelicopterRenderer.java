package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractHelicopterRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.SofacopterEntity;
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
public class SofaHelicopterRenderer extends AbstractHelicopterRenderer<SofacopterEntity>
{
    public SofaHelicopterRenderer(EntityType<SofacopterEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable SofacopterEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        matrixStack.pushPose();
        this.renderDamagedPart(vehicle, VehicleModels.RED_SOFA, matrixStack, renderTypeBuffer, light, partialTicks);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(0.0, 8 * 0.0625, 0.0);
        this.renderDamagedPart(vehicle, VehicleModels.SOFA_HELICOPTER_ARM, matrixStack, renderTypeBuffer, light, partialTicks);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(0.0, 32 * 0.0625, 0.0);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(this.bladeRotationProperty.get(vehicle, partialTicks)));
        matrixStack.scale(1.5F, 1.5F, 1.5F);
        this.renderDamagedPart(vehicle, VehicleModels.CEILING_FAN, matrixStack, renderTypeBuffer, light, partialTicks);
        matrixStack.popPose();

       /* GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getRenderItem().renderItem(entity.skid, ItemCameraTransforms.TransformType.NONE);
        GlStateManager.popMatrix();*/
    }

    @Override
    public void applyPlayerModel(SofacopterEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        model.rightArm.xRot = (float) Math.toRadians(-55F);
        model.rightArm.yRot = (float) Math.toRadians(25F);
        model.leftArm.xRot = (float) Math.toRadians(-55F);
        model.leftArm.yRot = (float) Math.toRadians(-25F);
        model.rightLeg.xRot = (float) Math.toRadians(-90F);
        model.rightLeg.yRot = (float) Math.toRadians(15F);
        model.leftLeg.xRot = (float) Math.toRadians(-90F);
        model.leftLeg.yRot = (float) Math.toRadians(-15F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.RED_SOFA, parts, transforms,
                    MatrixTransform.rotate(Axis.POSITIVE_Y.rotationDegrees(90F)));
            TransformHelper.createTransformListForPart(VehicleModels.SOFA_HELICOPTER_ARM, parts, transforms,
                    MatrixTransform.translate(0.0F, 8 * 0.0625F, 0.0F));
            TransformHelper.createFuelFillerTransforms(ModEntities.SOFACOPTER.get(), VehicleModels.SOFACOPTER_FUEL_DOOR_CLOSED, parts, transforms);
            TransformHelper.createIgnitionTransforms(ModEntities.SOFACOPTER.get(), parts, transforms);
        };
    }
}