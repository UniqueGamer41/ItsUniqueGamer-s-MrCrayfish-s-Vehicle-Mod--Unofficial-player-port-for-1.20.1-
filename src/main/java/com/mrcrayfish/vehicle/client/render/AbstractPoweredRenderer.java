package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.VehicleRayTraceResult;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.FuelFillerType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.util.port.Quaternion;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractPoweredRenderer<T extends PoweredVehicleEntity> extends AbstractVehicleRenderer<T>
{
    protected final PropertyFunction<T, ItemStack> engineStackProperty = new PropertyFunction<>(PoweredVehicleEntity::getEngineStack, () -> ItemStack.EMPTY);
    protected final PropertyFunction<T, Boolean> renderFuelPortProperty = new PropertyFunction<>(PoweredVehicleEntity::shouldRenderFuelPort, () -> true);
    protected final PropertyFunction<T, Float> wheelAngleProperty = new PropertyFunction<>(PoweredVehicleEntity::getRenderWheelAngle, () -> 0F);
    protected final PropertyFunction<T, Boolean> requiresEnergyProperty = new PropertyFunction<>(PoweredVehicleEntity::requiresEnergy, () -> false);
    protected final PropertyFunction<T, FuelFillerType> fuelFillerTypeProperty = new PropertyFunction<>(PoweredVehicleEntity::getFuelFillerType, () -> FuelFillerType.DEFAULT);
    protected final PropertyFunction<T, Boolean> needsKeyProperty = new PropertyFunction<>(PoweredVehicleEntity::isKeyNeeded, () -> false);

    public AbstractPoweredRenderer(EntityType<T> type, Supplier<VehicleProperties> defaultProperties)
    {
        super(type, defaultProperties);
    }

    public void setEngineStack(Supplier<ItemStack> engine)
    {
        this.engineStackProperty.setDefaultValue(engine);
    }

    public void setRenderFuelPort(boolean renderFuelPort)
    {
        this.renderFuelPortProperty.setDefaultValue(() -> renderFuelPort);
    }

    public void setWheelAngle(float angle)
    {
        this.wheelAngleProperty.setDefaultValue(() -> angle);
    }

    protected void renderEngine(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float delta, int light)
    {
        VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
        if(properties.getExtended(PoweredProperties.class).isRenderEngine() && !this.engineStackProperty.get(vehicle).isEmpty())
        {
            ItemStack engine = this.engineStackProperty.get(vehicle);
            if(!engine.isEmpty())
            {
                matrixStack.pushPose();
                {
                    if(vehicle != null && vehicle.isEnginePowered() && vehicle.getControllingPassenger() != null)
                    {
                        Quaternion rotation = new Quaternion(0F, 0F, 0F, 1F);
                        rotation.mul(Vector3f.XP.rotationDegrees(0.5F * (vehicle.tickCount % 2)));
                        rotation.mul(Vector3f.ZP.rotationDegrees(0.5F * (vehicle.tickCount % 2)));
                        rotation.mul(Vector3f.YP.rotationDegrees(-0.5F * (vehicle.tickCount % 2)));

                        matrixStack.mulPose(rotation);
                    }

                    BakedModel engineModel = RenderObjectHelper.getModel(this.engineStackProperty.get(vehicle));
                    Transform engineTransform = properties.getExtended(PoweredProperties.class).getEngineTransform();
                    matrixStack.translate(0.0, 0.5 * engineTransform.getScale(), 0.0);
                    this.renderPart(engineTransform, engineModel, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
                }
                matrixStack.popPose();
            }
        }
    }

    protected void renderFuelFiller(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light)
    {
        if(this.renderFuelPortProperty.get(vehicle) && this.requiresEnergyProperty.get(vehicle))
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            FuelFillerType fuelFillerType = this.fuelFillerTypeProperty.get(vehicle);
            VehicleRayTraceResult result = EntityRayTracer.instance().getContinuousInteraction();
            if(result != null && result.getType() == HitResult.Type.ENTITY && result.getEntity() == vehicle && result.equalsContinuousInteraction(RayTraceFunction.FUNCTION_FUELING))
            {
                this.renderPart(properties.getExtended(PoweredProperties.class).getFuelFillerTransform(), ((ComponentModel) fuelFillerType.getOpenModel().get()).getBaseModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
                if(this.shouldRenderFuelLid())
                {
                    //this.renderPart(properties.getFuelPortLidPosition(), entity.fuelPortLid);
                }
                vehicle.playFuelPortOpenSound();
            }
            else
            {
                this.renderPart(properties.getExtended(PoweredProperties.class).getFuelFillerTransform(), ((ComponentModel) fuelFillerType.getClosedModel().get()).getBaseModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
                vehicle.playFuelPortCloseSound();
            }
        }
    }

    protected void renderIgnition(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light)
    {
        if(this.needsKeyProperty.get(vehicle))
        {
            VehicleProperties properties = this.vehiclePropertiesProperty.get(vehicle);
            this.renderPart(properties.getExtended(PoweredProperties.class).getIgnitionTransform(), this.getKeyHoleModel().getBaseModel(), matrixStack, renderTypeBuffer, vehicle.getColor(), light, OverlayTexture.NO_OVERLAY);
            if(!vehicle.getKeyStack().isEmpty())
            {
                this.renderKey(vehicle, properties.getExtended(PoweredProperties.class).getIgnitionTransform(), vehicle.getKeyStack(), RenderObjectHelper.getModel(vehicle.getKeyStack()), matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    @Override
    protected void renderWheel(@Nullable T vehicle, Wheel wheel, ItemStack stack, BakedModel model, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light)
    {
        if(!wheel.shouldRender())
            return;

        matrixStack.pushPose();
        {
            matrixStack.translate((wheel.getOffsetX() * 0.0625) * wheel.getSide().getOffset(), wheel.getOffsetY() * 0.0625, wheel.getOffsetZ() * 0.0625);

            Quaternion rotation = new Quaternion(0, 0, 0, 1F);

            if(wheel.getPosition() == Wheel.Position.FRONT)
            {
                float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
                rotation.mul(Vector3f.YP.rotationDegrees(wheelAngle));
            }

            rotation.mul(Vector3f.XP.rotationDegrees(-this.getWheelRotation(vehicle, wheel, partialTicks)));

            if(wheel.getSide() != Wheel.Side.NONE)
            {
                matrixStack.translate((((wheel.getWidth() * wheel.getScaleX()) / 2) * 0.0625) * wheel.getSide().getOffset(), 0.0, 0.0);
            }

            matrixStack.scale(wheel.getScaleX(), wheel.getScaleY(), wheel.getScaleZ());

            if(wheel.getSide() == Wheel.Side.RIGHT)
            {
                rotation.mul(Vector3f.YP.rotationDegrees(180F));
            }

            matrixStack.mulPose(rotation);
            RenderObjectHelper.renderColoredModel(model, ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, IDyeable.getColorFromStack(stack), OverlayTexture.NO_OVERLAY, light);
        }
        matrixStack.popPose();
    }

    protected void renderSteeringWheel(T vehicle, ComponentModel model, double x, double y, double z, float scale, float angle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        matrixStack.pushPose();
        {
            float wheelAngle = this.wheelAngleProperty.get(vehicle, partialTicks);
            float maxSteeringAngle = this.vehiclePropertiesProperty.get(vehicle).getExtended(PoweredProperties.class).getMaxSteeringAngle();
            float steeringWheelRotation = (wheelAngle / maxSteeringAngle) * 25F;

            matrixStack.translate(x * 0.0625, y * 0.0625, z * 0.0625);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(angle));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(steeringWheelRotation));

            this.renderDamagedPart(vehicle, model, matrixStack, renderTypeBuffer, light, partialTicks);
        }
        matrixStack.popPose();
    }
}