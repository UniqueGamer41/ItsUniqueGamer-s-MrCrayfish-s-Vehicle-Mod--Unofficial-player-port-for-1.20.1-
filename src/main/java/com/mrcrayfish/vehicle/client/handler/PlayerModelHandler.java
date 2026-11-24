package com.mrcrayfish.vehicle.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.posture.api.event.PlayerModelEvent;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class PlayerModelHandler
{
    /**
     * Applies transformations to the player model when riding a vehicle and performing a wheelie
     */
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onPreRender(PlayerModelEvent.Render.Pre event)
    {
        Player player = event.getEntity();
        Entity ridingEntity = player.getVehicle();
        if(ridingEntity instanceof VehicleEntity vehicle)
        {
            this.applyPassengerTransformations(vehicle, player, event.getPoseStack(), event.getVertexConsumer(), event.getDeltaTicks());
            this.applyWheelieTransformations(vehicle, player, event.getPoseStack(), event.getDeltaTicks());
        }
    }

    @SuppressWarnings("unchecked")
    private void applyPassengerTransformations(VehicleEntity vehicle, Player player, PoseStack matrixStack, VertexConsumer builder, float partialTicks)
    {
        AbstractVehicleRenderer<VehicleEntity> render = (AbstractVehicleRenderer<VehicleEntity>) VehicleRenderRegistry.getRenderer(vehicle.getType());
        if(render != null)
        {
            render.applyPlayerRender(vehicle, player, partialTicks, matrixStack, builder);
        }
    }

    /**
     * Applies transformations to the player model when the vehicle is performing a wheelie
     *
     * @param vehicle      the vehicle performing the wheelie
     * @param player       the player riding in the vehicle
     * @param matrixStack  the current matrix stack
     * @param partialTicks the current partial ticks
     */
    private void applyWheelieTransformations(VehicleEntity vehicle, Player player, PoseStack matrixStack, float partialTicks)
    {
        if(!(vehicle instanceof LandVehicleEntity landVehicle))
            return;

        if(!landVehicle.canWheelie())
            return;

        int seatIndex = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
        if(seatIndex == -1)
            return;

        VehicleProperties properties = landVehicle.getProperties();
        Seat seat = properties.getSeats().get(seatIndex);
        Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).scale(0.0625);
        double vehicleScale = properties.getBodyTransform().getScale();
        double playerScale = 32.0 / 30.0;
        double offsetX = -(seatVec.x * playerScale);
        double offsetY = (seatVec.y + player.getMyRidingOffset()) * playerScale + 24 * 0.0625 - properties.getWheelOffset() * 0.0625 * vehicleScale;
        double offsetZ = (seatVec.z * playerScale) - landVehicle.getRearAxleOffset().z * 0.0625 * vehicleScale;
        matrixStack.translate(offsetX, offsetY, offsetZ);
        float p = landVehicle.getWheelieProgress(partialTicks);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-30F * landVehicle.getBoostStrength() * p));
        matrixStack.translate(-offsetX, -offsetY, -offsetZ);
    }

    public static void onSetupAngles(Player player, PlayerModel<Player> model, float partialTick) // FIXME
    {
        if(player.equals(Minecraft.getInstance().player) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
            return;

        if(ModDataKeys.GAS_PUMP.getValue(player).isPresent())
        {
            FuelingHandler.applyFuelingPose(player, model);
            return;
        }

        SprayCanHandler.applySprayCanPose(player, model);
        applyPassengerPose(player, model, partialTick);
    }

    /**
     * Applies a pose to the player model when the player is riding a vehicle. The pose varies
     * depending on the vehicle they are riding.
     *
     * @param player the player riding the vehicle
     * @param model the model of the player
     * @param partialTicks the current partial ticks
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyPassengerPose(Player player, PlayerModel model, float partialTicks)
    {
        Entity ridingEntity = player.getVehicle();
        if(!(ridingEntity instanceof VehicleEntity))
            return;

        VehicleEntity vehicle = (VehicleEntity) ridingEntity;
        AbstractVehicleRenderer<VehicleEntity> render = (AbstractVehicleRenderer<VehicleEntity>) VehicleRenderRegistry.getRenderer((EntityType<? extends VehicleEntity>) vehicle.getType());
        if(render != null)
        {
            render.applyPlayerModel(vehicle, player, model, partialTicks);
        }
    }
}
