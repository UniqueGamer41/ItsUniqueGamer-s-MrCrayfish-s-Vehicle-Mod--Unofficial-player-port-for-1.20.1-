package com.mrcrayfish.vehicle.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.render.RenderObjectHelper;
import com.mrcrayfish.vehicle.client.model.complex.ComplexModel;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class ComponentModel implements IComplexModel
{
    private final ResourceLocation modelLocation;
    @Nullable
    private BakedModel cachedModel;
    @Nullable
    private ComplexModel complexModel;

    public ComponentModel(ResourceLocation modelLocation)
    {
        this.modelLocation = modelLocation;
    }

    @Override
    public ResourceLocation getModelLocation()
    {
        return this.modelLocation;
    }

    @Override
    public BakedModel getBaseModel()
    {
        if(this.cachedModel == null)
        {
            this.cachedModel = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
        }
        return this.cachedModel;
    }

    public void clearCache()
    {
        this.cachedModel = null;
    }

    void setComplexModel(@Nullable ComplexModel complexModel)
    {
        this.complexModel = complexModel;
    }

    @Override
    @Nullable
    public ComplexModel getComplexModel()
    {
        return this.complexModel;
    }

    public void render(VehicleEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int color, int light, float partialTicks)
    {
        if(this.complexModel != null)
        {
            this.complexModel.render(entity, matrixStack, renderTypeBuffer, partialTicks, color, light);
        }
        else
        {
            RenderObjectHelper.renderColoredModel(this.getBaseModel(), ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, color, OverlayTexture.NO_OVERLAY, light);
        }
    }
}
