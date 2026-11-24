package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.client.model.baked.BakedModelSteepRamp;
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

/**
 * Author: MrCrayfish
 */
public class ModelSteepRamp implements IUnbakedGeometry<ModelSteepRamp>
{
    public static final IGeometryLoader<ModelSteepRamp> LOADER = (json, context) -> new ModelSteepRamp();

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        Function<ResourceLocation, TextureAtlasSprite> textureGetter =
                location -> spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, location));

        return new BakedModelSteepRamp(textureGetter, context.getTransforms());
    }
}