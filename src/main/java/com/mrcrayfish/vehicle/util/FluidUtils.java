package com.mrcrayfish.vehicle.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.client.render.RenderTypes;
import com.mrcrayfish.vehicle.client.render.util.ColorHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.EnumMap;

/**
 * Author: MrCrayfish
 */
public class FluidUtils
{
    private static final Object2IntMap<ResourceLocation> CACHE_FLUID_COLOR = Util.make(() -> {
        Object2IntMap<ResourceLocation> map = new Object2IntOpenHashMap<>();
        map.defaultReturnValue(-1);
        return map;
    });

    @OnlyIn(Dist.CLIENT)
    public static void clearCacheFluidColor()
    {
        CACHE_FLUID_COLOR.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static int getAverageFluidColor(Fluid fluid)
    {
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluid);
        int color = CACHE_FLUID_COLOR.getInt(key);

        if(color == -1)
        {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(fluid).getStillTexture());
            if(sprite != null)
            {
                long red = 0;
                long green = 0;
                long blue = 0;

                int area = sprite.contents().width() * sprite.contents().height();

                int maxX = sprite.contents().width();
                int maxY = sprite.contents().height();

                int prevRed, prevGreen, prevBlue;
                for(int y = 0; y < maxY; y++)
                {
                    for(int x = 0; x < maxX; x++)
                    {
                        int pixelColor = sprite.getPixelRGBA(0, x, y);

                        prevRed = ColorHelper.unpackABGRRed(pixelColor);
                        prevGreen = ColorHelper.unpackABGRGreen(pixelColor);
                        prevBlue = ColorHelper.unpackABGRBlue(pixelColor);

                        red += prevRed * prevRed;
                        green += prevGreen * prevGreen;
                        blue += prevBlue * prevBlue;
                    }
                }
                CACHE_FLUID_COLOR.put(key, color = ColorHelper.packARGB((int) Math.sqrt(red / area), (int) Math.sqrt(green / area), (int) Math.sqrt(blue / area), 130));
            }
        }

        return color;
    }

    public static int transferFluid(IFluidHandler source, IFluidHandler target, int maxAmount)
    {
        FluidStack drained = source.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
        if(drained.getAmount() > 0)
        {
            int filled = target.fill(drained, IFluidHandler.FluidAction.SIMULATE);
            if(filled > 0)
            {
                drained = source.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                return target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawFluidTankInGUI(FluidStack fluid, double x, double y, double percent, int height)
    {
        if(fluid == null || fluid.isEmpty())
            return;

        ResourceLocation key = IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture();
        int waterColor = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor();

        float red = ColorHelper.normalize(ColorHelper.unpackARGBRed(waterColor));
        float green = ColorHelper.normalize(ColorHelper.unpackARGBGreen(waterColor));
        float blue = ColorHelper.normalize(ColorHelper.unpackARGBBlue(waterColor));

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(key);
        if(sprite != null)
        {
            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();

            float deltaV = maxV - minV;
            double tankLevel = percent * height;

            Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

            int count = 1 + ((int) Math.ceil(tankLevel)) / 16;
            for(int i = 0; i < count; i++)
            {
                double subHeight = Math.min(16.0, tankLevel - (16.0 * i));
                double offsetY = height - 16.0 * i - subHeight;
                drawQuad(x, y + offsetY, 16, subHeight, minU, (float) (maxV - deltaV * (subHeight / 16.0)), maxU, maxV, red, green, blue);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawQuad(double x, double y, double width, double height, float minU, float minV, float maxU, float maxV, float red, float green, float blue)
    {
        //RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex((float) x, (float) (y + height), 0).uv(minU, maxV).color(red, green, blue, 1F).endVertex();
        buffer.vertex((float) (x + width), (float) (y + height), 0).uv(maxU, maxV).color(red, green, blue, 1F).endVertex();
        buffer.vertex((float) (x + width), (float) y, 0).uv(maxU, minV).color(red, green, blue, 1F).endVertex();
        buffer.vertex((float) x, (float) y, 0).uv(minU, minV).color(red, green, blue, 1F).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
        //RenderSystem.enableTexture();
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawFluidInWorld(FluidTank tank, Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float x, float y, float z, float width, float height, float depth, int light, FluidSides sides)
    {
        if(tank.isEmpty())
            return;

        ResourceLocation texture = IClientFluidTypeExtensions.of(tank.getFluid().getFluid()).getStillTexture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        int waterColor = IClientFluidTypeExtensions.of(tank.getFluid().getFluid()).getTintColor(tank.getFluid().getFluid().defaultFluidState(), world, pos);

        float red = ColorHelper.normalize(ColorHelper.unpackARGBRed(waterColor));
        float green = ColorHelper.normalize(ColorHelper.unpackARGBGreen(waterColor));
        float blue = ColorHelper.normalize(ColorHelper.unpackARGBBlue(waterColor));

        float side = 0.9F;
        float minU = sprite.getU0();
        float maxU = Math.min(minU + (sprite.getU1() - minU) * depth, sprite.getU1());
        float minV = sprite.getV0();
        float maxV = Math.min(minV + (sprite.getV1() - minV) * height, sprite.getV1());

        VertexConsumer buffer = renderTypeBuffer.getBuffer(RenderTypes.CUTOUT_TRANSPARENT_MIPPED);
        Matrix4f matrix = matrixStack.last().pose();

        //left side
        if(sides.test(Direction.WEST))
        {
            buffer.vertex(matrix, x + width, y, z)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(maxU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y, z)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(minU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y + height, z)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(minU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(maxU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }

        //right side
        if(sides.test(Direction.EAST))
        {
            buffer.vertex(matrix, x, y, z + depth)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(maxU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y, z + depth)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(minU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z + depth)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(minU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y + height, z + depth)
                    .color(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F)
                    .uv(maxU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }

        maxU = Math.min(minU + (sprite.getU1() - minU) * depth, sprite.getU1());

        if(sides.test(Direction.SOUTH))
        {
            buffer.vertex(matrix, x + width, y, z + depth)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(maxU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y, z)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(minU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(minU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z + depth)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(maxU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }

        if(sides.test(Direction.NORTH))
        {
            buffer.vertex(matrix, x, y, z)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(minU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y, z + depth)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(maxU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y + height, z + depth)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(maxU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y + height, z)
                    .color(red * side, green * side, blue * side, 1.0F)
                    .uv(minU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }

        maxV = Math.min(minV + (sprite.getV1() - minV) * width, sprite.getV1());

        if(sides.test(Direction.UP))
        {
            buffer.vertex(matrix, x, y + height, z)
                    .color(red, green, blue, 1.0F)
                    .uv(maxU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x, y + height, z + depth)
                    .color(red, green, blue, 1.0F)
                    .uv(minU, minV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z + depth)
                    .color(red, green, blue, 1.0F)
                    .uv(minU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();

            buffer.vertex(matrix, x + width, y + height, z)
                    .color(red, green, blue, 1.0F)
                    .uv(maxU, maxV)
                    .uv2(light)
                    .normal(0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
    }

    public static class FluidSides
    {
        private final EnumMap<Direction, Boolean> map = new EnumMap<>(Direction.class);

        public FluidSides(Direction ... sides)
        {
            for(Direction direction : Direction.values())
            {
                this.map.put(direction, false);
            }

            for(Direction side : sides)
            {
                this.map.put(side, true);
            }
        }

        public boolean test(Direction direction)
        {
            return this.map.get(direction);
        }
    }
}
