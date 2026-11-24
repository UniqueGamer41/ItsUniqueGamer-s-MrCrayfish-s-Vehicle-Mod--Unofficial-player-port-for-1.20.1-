package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.client.model.baked.BakedModelRamp;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class ModelRamp implements IUnbakedGeometry<ModelRamp>
{
    public static final IGeometryLoader<ModelRamp> LOADER = (json, context) -> new ModelRamp();

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        Function<ResourceLocation, TextureAtlasSprite> textureGetter =
                location -> spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, location));

        return new BakedModelRamp(textureGetter, context.getTransforms());
    }
}