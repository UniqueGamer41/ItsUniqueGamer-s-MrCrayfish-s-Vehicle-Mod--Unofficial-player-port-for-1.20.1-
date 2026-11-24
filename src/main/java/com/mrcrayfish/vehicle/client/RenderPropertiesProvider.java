package com.mrcrayfish.vehicle.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.render.util.ColorHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class RenderPropertiesProvider
{
    public static final IClientFluidTypeExtensions BLAZE_JUICE = new IClientFluidTypeExtensions()
    {
        static final ResourceLocation STILL_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/blaze_juice_still");
        static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/blaze_juice_flowing");
        static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/blaze_juice_overlay");
        static final int PACKED_COLOR = ColorHelper.packARGB(254, 198, 0, 0xFF);
        static final Vector3f COLOR = new Vector3f(0.9960784313725490196078431372549F, 0.77647058823529411764705882352941F, 0F);

        @Override
        public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape)
        {
            modifyCommonFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
        }

        @Override
        public ResourceLocation getStillTexture()
        {
            return STILL_TEXTURE;
        }

        @Override
        public ResourceLocation getFlowingTexture()
        {
            return FLOWING_TEXTURE;
        }

        @Override
        public ResourceLocation getOverlayTexture()
        {
            return OVERLAY_TEXTURE;
        }

        @Override
        @NotNull
        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor)
        {
            return COLOR;
        }

        @Override
        public int getTintColor()
        {
            return PACKED_COLOR;
        }
    };

    public static final IClientFluidTypeExtensions ENDER_SAP = new IClientFluidTypeExtensions()
    {
        static final ResourceLocation STILL_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/ender_sap_still");
        static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/ender_sap_flowing");
        static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/ender_sap_overlay");
        static final int PACKED_COLOR = ColorHelper.packARGB(10, 93, 80, 0xFF);
        static final Vector3f COLOR = new Vector3f(0.03921568627450980392156862745098F, 0.36470588235294117647058823529412F, 0.31372549019607843137254901960784F);

        @Override
        public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape)
        {
            modifyCommonFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
        }

        @Override
        public ResourceLocation getStillTexture()
        {
            return STILL_TEXTURE;
        }

        @Override
        public ResourceLocation getFlowingTexture()
        {
            return FLOWING_TEXTURE;
        }

        @Override
        public ResourceLocation getOverlayTexture()
        {
            return OVERLAY_TEXTURE;
        }


        @Override
        @NotNull
        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor)
        {
            return COLOR;
        }

        @Override
        public int getTintColor()
        {
            return PACKED_COLOR;
        }
    };

    public static final IClientFluidTypeExtensions FUELIUM = new IClientFluidTypeExtensions()
    {
        static final ResourceLocation STILL_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/fuelium_still");
        static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/fuelium_flowing");
        static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Reference.MOD_ID, "block/fuelium_overlay");
        static final int PACKED_COLOR = ColorHelper.packARGB(148, 242, 45, 0xFF);
        static final Vector3f COLOR = new Vector3f(0.58039215686274509803921568627451F, 0.94901960784313725490196078431373F, 0.17647058823529411764705882352941F);

        @Override
        public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape)
        {
            modifyCommonFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
        }

        @Override
        public ResourceLocation getStillTexture()
        {
            return STILL_TEXTURE;
        }

        @Override
        public ResourceLocation getFlowingTexture()
        {
            return FLOWING_TEXTURE;
        }

        @Override
        public ResourceLocation getOverlayTexture()
        {
            return OVERLAY_TEXTURE;
        }

        @Override
        @NotNull
        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor)
        {
            return COLOR;
        }

        @Override
        public int getTintColor()
        {
            return PACKED_COLOR;
        }
    };

    protected static void modifyCommonFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape)
    {
        Entity entity = camera.getEntity();

        nearDistance = -8.0F;
        farDistance = 96.0F;

        if (entity instanceof LocalPlayer player)
        {
            farDistance *= Math.max(0.25F, player.getWaterVision());

            Holder<Biome> holder = player.level().getBiome(player.blockPosition());
            if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG))
            {
                farDistance *= 0.85F;
            }
        }

        if (farDistance > renderDistance)
        {
            farDistance = renderDistance;
            shape = FogShape.CYLINDER;
        }

        RenderSystem.setShaderFogStart(nearDistance);
        RenderSystem.setShaderFogEnd(farDistance);
        RenderSystem.setShaderFogShape(shape);
    }
}
