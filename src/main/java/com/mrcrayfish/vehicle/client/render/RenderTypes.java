package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderTypes
{
    public static final RenderType CUTOUT_TRANSPARENT_MIPPED = RenderType.create("vehicle:cutout_transparent_mipped",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder()
                    .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeCutoutMippedShader))
                    .setTextureState(
                            RenderStateShard.MultiTextureStateShard.builder()
                                    .add(InventoryMenu.BLOCK_ATLAS, false, true)
                                    .build()
                    )
                    .setTransparencyState(
                            new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                                    RenderSystem.enableBlend();
                                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                            }, () -> {
                                    RenderSystem.disableBlend();
                                    RenderSystem.defaultBlendFunc();
                            })
                    )
                    .createCompositeState(true)
    );

    public static final RenderType FUEL_DRUM_FLUID_LABEL = RenderType.create("vehicle:fuel_drum_label_fluid",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
                    .setTextureState(
                            RenderStateShard.MultiTextureStateShard.builder()
                                    .add(InventoryMenu.BLOCK_ATLAS, false, true)
                                    .build()
                    )
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader))
                    .createCompositeState(false)
    );

    public static final RenderType FUEL_DRUM_FLUID_LABEL_BACKGROUND = RenderType.create("vehicle:fuel_drum_label_background",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .createCompositeState(false)
    );
}
