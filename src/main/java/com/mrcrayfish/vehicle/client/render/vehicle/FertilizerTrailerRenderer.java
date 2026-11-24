package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractTrailerRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.RenderObjectHelper;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.trailer.FertilizerTrailerEntity;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class FertilizerTrailerRenderer extends AbstractTrailerRenderer<FertilizerTrailerEntity>
{
    protected final PropertyFunction<FertilizerTrailerEntity, StorageInventory> storageProperty = new PropertyFunction<>(FertilizerTrailerEntity::getInventory, () -> null);

    public FertilizerTrailerRenderer(EntityType<FertilizerTrailerEntity> type)
    {
        super(type, () -> VehicleProperties.get(type));
    }

    @Override
    protected void render(@Nullable FertilizerTrailerEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.FERTILIZER_TRAILER, matrixStack, renderTypeBuffer, light, partialTicks);

        StorageInventory inventory = this.storageProperty.get(vehicle);
        if(inventory != null)
        {
            int layer = 0;
            int index = 0;
            for(int i = 0; i < inventory.getContainerSize(); i++)
            {
                ItemStack stack = inventory.getItem(i);
                if(!stack.isEmpty())
                {
                    matrixStack.pushPose();
                    matrixStack.translate(-5.5 * 0.0625, -3 * 0.0625, -3 * 0.0625);
                    matrixStack.scale(0.45F, 0.45F, 0.45F);

                    int count = Math.max(1, stack.getCount() / 32);
                    int width = 3;
                    int maxLayerCount = 6;
                    for(int j = 0; j < count; j++)
                    {
                        matrixStack.pushPose();
                        {
                            int layerIndex = index % maxLayerCount;
                            matrixStack.translate(0, layer * 0.1 + j * 0.0625, 0);
                            matrixStack.translate((layerIndex % width) * 0.5, 0, (float) (layerIndex / width) * 0.75);
                            matrixStack.translate(0.5 * (layer % 2), 0, 0);
                            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(47F * index));
                            matrixStack.mulPose(Vector3f.XP.rotationDegrees(2F * layerIndex));
                            matrixStack.translate(layer * 0.001, layer * 0.001, layer * 0.001); // Fixes Z fighting
                            MINECRAFT.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, RenderObjectHelper.getModel(stack));
                        }
                        matrixStack.popPose();
                        index++;
                        if(index % maxLayerCount == 0)
                        {
                            layer++;
                        }
                    }
                    matrixStack.popPose();
                }
            }
        }

        /* Renders the spike */
        matrixStack.pushPose();
        {
            matrixStack.translate(0, -0.5, -0.4375);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90F));
            matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-this.getWheelRotation(vehicle, null, partialTicks)));
            matrixStack.scale((float) 1.25, (float) 1.25, (float) 1.25);
            RenderObjectHelper.renderColoredModel(VehicleModels.SEED_SPIKER.getBaseModel(), ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, -1, OverlayTexture.NO_OVERLAY, light);
        }
        matrixStack.popPose();
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.FERTILIZER_TRAILER, parts, transforms);
        };
    }
}
