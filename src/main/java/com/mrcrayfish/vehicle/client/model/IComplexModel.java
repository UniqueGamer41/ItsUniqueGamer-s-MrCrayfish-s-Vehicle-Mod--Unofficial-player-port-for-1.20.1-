package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.client.model.complex.ComplexModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public interface IComplexModel
{
    ResourceLocation getModelLocation();

    BakedModel getBaseModel();

    @Nullable
    ComplexModel getComplexModel();
}
