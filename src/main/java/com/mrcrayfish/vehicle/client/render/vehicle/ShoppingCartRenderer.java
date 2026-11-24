package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractLandVehicleRenderer;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.ShoppingCartEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;

/**
 * Author: MrCrayfish
 */
public class ShoppingCartRenderer extends AbstractLandVehicleRenderer<ShoppingCartEntity>
{
    public ShoppingCartRenderer(EntityType<ShoppingCartEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable ShoppingCartEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.SHOPPING_CART_BODY, matrixStack, renderTypeBuffer, light, partialTicks);
    }

    @Override
    public void applyPlayerModel(ShoppingCartEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        model.rightArm.xRot = (float) Math.toRadians(-70F);
        model.rightArm.yRot = (float) Math.toRadians(5F);
        model.leftArm.xRot = (float) Math.toRadians(-70F);
        model.leftArm.yRot = (float) Math.toRadians(-5F);
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
            TransformHelper.createTransformListForPart(VehicleModels.SHOPPING_CART_BODY, parts, transforms);
        };
    }
}
