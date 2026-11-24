package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.common.CosmeticTracker;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.*;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.util.RenderUtil;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractVehicleRenderer<T extends VehicleEntity>
{
    protected static final Minecraft MINECRAFT = Minecraft.getInstance();
    protected final EntityType<T> type;
    protected final PropertyFunction<T, VehicleProperties> vehiclePropertiesProperty;
    protected final PropertyFunction<T, CosmeticTracker> cosmeticTrackerProperty;
    protected final PropertyFunction<T, Integer> colorProperty = new PropertyFunction<>(VehicleEntity::getColor, () -> -1);
    protected final PropertyFunction<T, Float> bodyYawProperty = new PropertyFunction<>(VehicleEntity::getBodyRotationYaw, () -> 0F);
    protected final PropertyFunction<T, Float> bodyPitchProperty = new PropertyFunction<>(VehicleEntity::getBodyRotationPitch, () -> 0F);
    protected final PropertyFunction<T, Float> bodyRollProperty = new PropertyFunction<>(VehicleEntity::getBodyRotationRoll, () -> 0F);
    protected final PropertyFunction<T, ItemStack> wheelStackProperty = new PropertyFunction<>(VehicleEntity::getWheelStack, () -> ItemStack.EMPTY);
    protected final PropertyFunction<Pair<T, Wheel>, Float> wheelRotationProperty = new PropertyFunction<>((p, f) -> p.getLeft().getWheelRotation(p.getRight(), f), () -> 0F);

    public AbstractVehicleRenderer(EntityType<T> type, Supplier<VehicleProperties> defaultProperties)
    {
        this.type = type;
        this.vehiclePropertiesProperty = new PropertyFunction<>(VehicleEntity::getProperties, defaultProperties);
        this.cosmeticTrackerProperty = new PropertyFunction<>(VehicleEntity::getCosmeticTracker, () -> null);
    }

    @Nullable
    public abstract RayTraceTransforms getRayTraceTransforms();

    protected abstract void render(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light);

    public void setupTransformsAndRender(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        matrixStack.pushPose();

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
            this.getTowBarModel().render(vehicle, matrixStack, renderTypeBuffer, this.colorProperty.get(vehicle), light, partialTicks);
            matrixStack.popPose();
        }

        // Fixes the origin
        matrixStack.translate(0, 0.5, 0);

        // Translate the vehicle so the center of the axles are touching the ground
        matrixStack.translate(0, properties.getAxleOffset() * 0.0625F, 0);

        // Translate the vehicle so it's actually riding on it's wheels
        matrixStack.translate(0, properties.getWheelOffset() * 0.0625F, 0);

        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) bodyPosition.getRotX()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) bodyPosition.getRotY()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) bodyPosition.getRotZ()));
        this.render(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
        matrixStack.popPose();

        this.renderWheels(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);

        matrixStack.popPose();
    }

    /**
     *
     * @param entity
     * @param partialTicks
     */
    public void applyPreRotations(T entity, PoseStack stack, float partialTicks) {}

    public void applyPlayerModel(T entity, Player player, PlayerModel<?> model, float partialTicks) {}

    public void applyPlayerRender(T entity, Player player, float partialTicks, PoseStack matrixStack, VertexConsumer buffers)
    {
        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
        if(index != -1)
        {
            VehicleProperties properties = entity.getProperties();
            Seat seat = properties.getSeats().get(index);
            Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(properties.getBodyTransform().getTranslate()).scale(0.0625);
            double playerScale = 32.0 / 30.0;
            double offsetX = -seatVec.x * playerScale;
            double offsetY = (seatVec.y + player.getMyRidingOffset()) * playerScale + (24 * 0.0625);
            double offsetZ = seatVec.z * playerScale;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-seat.getYawOffset()));
            matrixStack.translate(offsetX, offsetY, offsetZ);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(entity.getBodyRotationPitch(partialTicks)));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-entity.getBodyRotationRoll(partialTicks)));
            matrixStack.translate(-offsetX, -offsetY, -offsetZ);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(seat.getYawOffset()));
        }
    }

    protected void renderDamagedPart(@Nullable T vehicle, ComponentModel model, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        this.renderDamagedPart(vehicle, model, matrixStack, renderTypeBuffer, false, light, partialTicks);
        this.renderDamagedPart(vehicle, model, matrixStack, renderTypeBuffer, true, light, partialTicks);
    }

    private void renderDamagedPart(@Nullable T vehicle, ComponentModel model, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, boolean renderDamage, int light, float partialTicks)
    {
        if(renderDamage && vehicle != null)
        {
            if(vehicle.getDestroyedStage() > 0)
            {
                RenderUtil.renderDamagedVehicleModel(model.getBaseModel(), ItemDisplayContext.NONE, false, matrixStack, vehicle.getDestroyedStage(), this.colorProperty.get(vehicle), OverlayTexture.NO_OVERLAY, light);
            }
        }
        else
        {
            model.render(vehicle, matrixStack, renderTypeBuffer, this.colorProperty.get(vehicle), light, partialTicks);
        }
    }

    /**
     * Renders a part (ItemStack) on the vehicle using the specified PartPosition. The rendering
     * will be cancelled if the PartPosition parameter is null.
     *
     * @param position the render definitions to construct to the part
     * @param model the part to render onto the vehicle
     */
    protected void renderPart(Transform position, BakedModel model, PoseStack matrixStack, MultiBufferSource buffer, int color, int lightTexture, int overlayTexture)
    {
        if(position == null) return;
        matrixStack.pushPose();
        {
            matrixStack.translate(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
            matrixStack.translate(0.0, -0.5, 0.0);
            matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
            matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) position.getRotX()));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY()));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) position.getRotZ()));
            RenderObjectHelper.renderColoredModel(model, ItemDisplayContext.NONE, false, matrixStack, buffer, color, overlayTexture, lightTexture);
        }
        matrixStack.popPose();
    }

    protected void renderKey(T vehicle, Transform position, ItemStack stack, BakedModel model, PoseStack matrixStack, MultiBufferSource buffer, int color, int lightTexture, int overlayTexture)
    {
        if(position == null) return;
        matrixStack.pushPose();
        {
            matrixStack.translate(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
            if (vehicle instanceof AtvEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            } else if (vehicle instanceof BumperCarEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof CompactHelicopterEntity) {
                matrixStack.translate(-0.02, -0.48, 0.0);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 180F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof DirtBikeEntity) {
                matrixStack.translate(0.0, -0.48, -0.018);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof DuneBuggyEntity) {
                matrixStack.translate(0.0, -0.49, -0.0);
                matrixStack.scale(0.08F, 0.08F, 0.08F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof GoKartEntity) {
                matrixStack.translate(-0.02, -0.48, 0.0);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 180F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof GolfCartEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof JetSkiEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof LawnMowerEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof MiniBikeEntity) {
                matrixStack.translate(0.0, -0.48, -0.0);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof MiniBusEntity) {
                matrixStack.translate(0.0, -0.48, -0.02);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof MopedEntity) {
                matrixStack.translate(-0.02, -0.48, 0.0);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 180F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof OffRoaderEntity) {
                matrixStack.translate(0.0, -0.48, -0.02);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof QuadBikeEntity) {
                matrixStack.translate(0.0, -0.48, -0.02);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof SmartCarEntity) {
                matrixStack.translate(0.0, -0.48, -0.02);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof SpeedBoatEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof SportsCarEntity) {
                matrixStack.translate(-0.02, -0.48, 0.0);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 180F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof SportsPlaneEntity) {
                matrixStack.translate(0.0, -0.48, -0.02);
                matrixStack.scale(0.1F, 0.1F, 0.1F);

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }
            else if (vehicle instanceof TractorEntity) {
                matrixStack.translate(0.0, -0.48, -0.05);
                matrixStack.scale(0.1F, 0.1F, 0.1F); //TODO UNCOMMENT THIS BEFORE FURTHER TESTING TOMORROW

                matrixStack.mulPose(Vector3f.XP.rotationDegrees(30F));
                matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY() + 90F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(110F));
                matrixStack.translate(0.0, 0.0, -0.05);

                RenderObjectHelper.renderModel(stack, ItemDisplayContext.NONE, false, matrixStack, buffer, overlayTexture, lightTexture, model);

            }




        }
        matrixStack.popPose();
    }

    protected void renderWheels(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        ItemStack wheelStack = this.wheelStackProperty.get(vehicle);
        if(!wheelStack.isEmpty())
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            matrixStack.pushPose();
            {
                matrixStack.translate(0.0, -8 * 0.0625, 0.0);
                matrixStack.translate(0.0, -properties.getAxleOffset() * 0.0625F, 0.0);
                BakedModel wheelModel = RenderObjectHelper.getModel(wheelStack);

                for(Wheel wheel : properties.getWheels())
                {
                    if(!wheel.shouldRender())
                    {
                        return;
                    }

                    this.renderWheel(vehicle, wheel, wheelStack, wheelModel, partialTicks, matrixStack, renderTypeBuffer, light);
                }
            }
            matrixStack.popPose();
        }
    }

    protected void renderWheel(@Nullable T vehicle, Wheel wheel, ItemStack stack, BakedModel model, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light)
    {
        matrixStack.pushPose();
        {
            matrixStack.translate((wheel.getOffsetX() * 0.0625) * wheel.getSide().getOffset(), wheel.getOffsetY() * 0.0625, wheel.getOffsetZ() * 0.0625);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-this.getWheelRotation(vehicle, wheel, partialTicks)));

            if(wheel.getSide() != Wheel.Side.NONE)
            {
                matrixStack.translate((((wheel.getWidth() * wheel.getScaleX()) / 2) * 0.0625) * wheel.getSide().getOffset(), 0.0, 0.0);
            }

            matrixStack.scale(wheel.getScaleX(), wheel.getScaleY(), wheel.getScaleZ());

            if(wheel.getSide() == Wheel.Side.RIGHT)
            {
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            }

            RenderObjectHelper.renderColoredModel(model, ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, IDyeable.getColorFromStack(stack), OverlayTexture.NO_OVERLAY, light);
        }
        matrixStack.popPose();
    }

    protected void renderCosmetics(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
        properties.getCosmetics().forEach((id, cosmetic) -> {
            if(!this.canRenderCosmetic(vehicle, id)) return;
            this.getCosmeticModel(vehicle, id).ifPresent(model -> {
                Vec3 offset = cosmetic.getOffset().scale(0.0625);
                matrixStack.pushPose();
                matrixStack.translate(offset.x, offset.y, offset.z);
                matrixStack.translate(0, -0.5, 0);
                this.getCosmeticActions(vehicle, id).forEach(action -> action.beforeRender(matrixStack, vehicle, partialTicks));
                model.render(vehicle, matrixStack, renderTypeBuffer, this.colorProperty.get(vehicle), light, partialTicks); //TODO allow individual cosmetic colours
                matrixStack.popPose();
            });
        });
    }

    protected boolean canRenderCosmetic(@Nullable T vehicle, ResourceLocation cosmeticId)
    {
        if(vehicle != null)
        {
            VehicleProperties vehicleProperties = this.vehiclePropertiesProperty.get(vehicle);
            for(CosmeticProperties cosmeticProperties : vehicleProperties.getCosmetics().values())
            {
                CosmeticTracker tracker = this.cosmeticTrackerProperty.get(vehicle);
                ResourceLocation location = tracker.getSelectedModelLocation(cosmeticProperties.getId());
                Map<ResourceLocation, List<ResourceLocation>> disabledCosmetics = cosmeticProperties.getDisabledCosmetics();
                if(disabledCosmetics.containsKey(location) && disabledCosmetics.get(location).contains(cosmeticId))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    protected ResourceLocation getCosmeticModelLocation(@Nullable T vehicle, ResourceLocation cosmeticId)
    {
        if(vehicle != null)
        {
            CosmeticTracker.Entry entry = this.cosmeticTrackerProperty.get(vehicle).getSelectedEntry(cosmeticId);
            if(entry != null)
            {
                return entry.getModelLocation();
            }
        }
        CosmeticProperties properties = VehicleProperties.get(this.type).getCosmetics().get(cosmeticId);
        if(!properties.getModelLocations().isEmpty())
        {
            return properties.getModelLocations().get(0);
        }
        return null;
    }

    protected Optional<ComponentModel> getCosmeticModel(@Nullable T vehicle, ResourceLocation cosmeticId)
    {
        if(vehicle != null)
        {
            return Optional.ofNullable(this.cosmeticTrackerProperty.get(vehicle).getSelectedModel(cosmeticId));
        }
        CosmeticProperties properties = VehicleProperties.get(this.type).getCosmetics().get(cosmeticId);
        if(!properties.getModelLocations().isEmpty())
        {
            ResourceLocation modelLocation = properties.getModelLocations().get(0);
            return Optional.ofNullable(ComponentManager.lookupModel(modelLocation));
        }
        return Optional.empty();
    }

    protected Collection<Action> getCosmeticActions(@Nullable T vehicle, ResourceLocation cosmeticId)
    {
        if(vehicle != null)
        {
            return this.cosmeticTrackerProperty.get(vehicle).getActions(cosmeticId);
        }
        return Collections.emptyList();
    }

    protected ComponentModel getKeyHoleModel()
    {
        return VehicleModels.KEY_HOLE;
    }

    protected ComponentModel getTowBarModel()
    {
        return VehicleModels.TOW_BAR;
    }

    protected boolean shouldRenderFuelLid()
    {
        return true;
    }

    public void setVehicleProperties(Supplier<VehicleProperties> properties)
    {
        this.vehiclePropertiesProperty.setDefaultValue(properties);
    }

    public void setColor(int color)
    {
        this.colorProperty.setDefaultValue(() -> color);
    }

    public void setBodyYaw(float yaw)
    {
        this.bodyYawProperty.setDefaultValue(() -> yaw);
    }

    public void setBodyPitch(float pitch)
    {
        this.bodyPitchProperty.setDefaultValue(() -> pitch);
    }

    public void setBodyRoll(float roll)
    {
        this.bodyRollProperty.setDefaultValue(() -> roll);
    }

    public void setWheelStack(Supplier<ItemStack> wheel)
    {
        this.wheelStackProperty.setDefaultValue(wheel);
    }

    public void setWheelRotation(float rotation)
    {
        this.wheelRotationProperty.setDefaultValue(() -> rotation);
    }

    public float getWheelRotation(@Nullable T vehicle, @Nullable Wheel wheel, float partialTicks)
    {
        if(vehicle != null)
        {
            return this.wheelRotationProperty.get(Pair.of(vehicle, wheel), partialTicks);
        }
        return this.wheelRotationProperty.get();
    }

    public void setCosmeticTracker(Supplier<CosmeticTracker> tracker)
    {
        this.cosmeticTrackerProperty.setDefaultValue(tracker);
    }

    protected static class PropertyFunction<V, T>
    {
        protected BiFunction<V, Float, T> function;
        protected Supplier<T> defaultValue;

        public PropertyFunction(Function<V, T> function, Supplier<T> defaultValue)
        {
            this((v, p) -> function.apply(v), defaultValue);
        }

        public PropertyFunction(BiFunction<V, Float, T> function, Supplier<T> defaultValue)
        {
            this.function = function;
            this.defaultValue = defaultValue;
        }

        public T get()
        {
            return this.get(null);
        }

        public T get(@Nullable V vehicle)
        {
            return this.get(vehicle, 0F);
        }

        public T get(@Nullable V vehicle, float partialTicks)
        {
            return vehicle != null ? this.function.apply(vehicle, partialTicks) : this.defaultValue.get();
        }

        public void setDefaultValue(Supplier<T> value)
        {
            this.defaultValue = value;
        }
    }
}