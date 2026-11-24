package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractBoatRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.Seat;

import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.AluminumBoatEntity;
import com.mrcrayfish.vehicle.entity.vehicle.JetSkiEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Updated Renderer: Aluminum Boat for 1.19.4
 */
public class AluminumBoatRenderer extends AbstractBoatRenderer<AluminumBoatEntity>
{


    public AluminumBoatRenderer(EntityType<AluminumBoatEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));


    }

    @Override
    protected void render(@Nullable AluminumBoatEntity vehicle, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.ALUMINUM_BOAT_BODY, matrixStack, buffer, light, partialTicks);



    }

    @Override
    public void applyPlayerModel(AluminumBoatEntity entity, Player player, PlayerModel<?> model, float partialTicks)
    {
        model.rightLeg.xRot = (float) Math.toRadians(-85F);
        model.rightLeg.yRot = (float) Math.toRadians(20F);
        model.leftLeg.xRot = (float) Math.toRadians(-85F);
        model.leftLeg.yRot = (float) Math.toRadians(-20F);
    }

//    @Override
//    public void applyPlayerRender(AluminumBoatEntity entity, Player player, float partialTicks, PoseStack matrixStack) // REMOVED VertexConsumer
//    {
//        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
//        if(index != -1)
//        {
//            VehicleProperties properties = entity.getProperties();
//            Seat seat = properties.getSeats().get(index);
//
//            double x = -seat.getX();
//            double y = seat.getY() + entity.getVehicleYOffset() + player.getMyRidingOffset(); // vehicleYOffset replaces axle + wheel offset
//            double z = seat.getZ();
//
//            matrixStack.translate(x * 0.0625, y * 0.0625, z * 0.0625);
//
//            float currentSpeed = (float) (entity.prevCurrentSpeed + (entity.currentSpeed - entity.prevCurrentSpeed) * partialTicks);
//            float maxSpeed = entity.getMaxSpeed();
//            float currentSpeedNormal = currentSpeed / maxSpeed;
//
//            float turnAngle = (float) (entity.prevTurnAngle + (entity.turnAngle - entity.prevTurnAngle) * partialTicks);
//            float maxTurnAngle = entity.getMaxTurnAngle();
//            float turnAngleNormal = turnAngle / maxTurnAngle;
//
//            matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-8F * Math.min(1.0F, currentSpeedNormal)));
//            matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees(turnAngleNormal * currentSpeedNormal * 15F));
//
//            matrixStack.translate(-x * 0.0625, -y * 0.0625, -z * 0.0625);
//        }
//    }


    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) -> {
            TransformHelper.createTransformListForPart(VehicleModels.ALUMINUM_BOAT_BODY, parts, transforms);
            TransformHelper.createTransformListForPart(
                    VehicleModels.ALUMINUM_BOAT_FUEL_DOOR_CLOSED,
                    parts,
                    transforms,
                    RayTraceFunction.FUNCTION_FUELING,
                    MatrixTransform.translate(-8.0F * 0.0625F, 22.5F * 0.0625F, -8.0F * 0.0625F),
                    MatrixTransform.rotate(Axis.NEGATIVE_X.rotationDegrees(90F))
            );
        };
    }
}
