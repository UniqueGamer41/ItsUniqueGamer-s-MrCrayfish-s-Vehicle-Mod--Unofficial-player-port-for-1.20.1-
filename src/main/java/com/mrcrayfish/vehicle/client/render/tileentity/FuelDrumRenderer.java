package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.block.entity.FuelDrumBlockEntity;
import com.mrcrayfish.vehicle.client.render.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Author: MrCrayfish
 */
public class FuelDrumRenderer implements BlockEntityRenderer<FuelDrumBlockEntity>
{
    private final Font font;

    public FuelDrumRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.font = ctx.getFont();
    }

    @Override
    public void render(@NotNull FuelDrumBlockEntity entity, float delta, @NotNull PoseStack matrices, @NotNull MultiBufferSource buffers, int light, int overlay)
    {
        Minecraft minecraft = Minecraft.getInstance();
        HitResult hitResult = minecraft.hitResult;

        if(minecraft.player.isCrouching())
        {
            if(entity.hasFluid() && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult result = (BlockHitResult) hitResult;
                if(result.getBlockPos().equals(entity.getBlockPos()))
                {
                    this.drawFluidLabel(this.font, minecraft.getEntityRenderDispatcher().cameraOrientation(), entity.getFluidTank(), matrices, buffers);
                }
            }
        }
    }

    private void drawFluidLabel(Font fontRendererIn, Quaternionf rotation, FluidTank tank, PoseStack matrixStack, MultiBufferSource renderTypeBuffer)
    {
        if(tank.getFluid().isEmpty())
            return;

        FluidStack stack = tank.getFluid();
        ResourceLocation stillTexture = IClientFluidTypeExtensions.of(tank.getFluid().getFluid()).getStillTexture();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        if(sprite != null)
        {
            float level = tank.getFluidAmount() / (float) tank.getCapacity();
            float width = 30F;
            float fuelWidth = width * level;
            float remainingWidth = width - fuelWidth;
            float offsetWidth = width / 2.0F;

            matrixStack.pushPose();
            matrixStack.translate(0.5, 1.25, 0.5);
            matrixStack.mulPose(rotation);
            matrixStack.scale(-0.025F, -0.025F, 0.025F);

            VertexConsumer backgroundBuilder = renderTypeBuffer.getBuffer(RenderTypes.FUEL_DRUM_FLUID_LABEL_BACKGROUND);

            /* Background */
            Matrix4f matrix = matrixStack.last().pose();
            backgroundBuilder.vertex(matrix, -offsetWidth - 1.0F, -2.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth - 1.0F, 5.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + width + 1.0F, 5.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + width + 1.0F, -2.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();

            matrixStack.translate(0, 0, -0.05);

            /* Remaining */
            matrix = matrixStack.last().pose();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth + remainingWidth, 4.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth + remainingWidth, -1.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();

            float minU = sprite.getU0();
            float maxU = minU + (sprite.getU1() - minU) * level;
            float minV = sprite.getV0();
            float maxV = minV + (sprite.getV1() - minV) * 4 * 0.0625F;

            /* Fluid Texture */
            VertexConsumer fluidBuilder = renderTypeBuffer.getBuffer(RenderTypes.FUEL_DRUM_FLUID_LABEL);
            fluidBuilder.vertex(matrix, -offsetWidth, -1.0F, 0.0F).uv(minU, maxV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth, 4.0F, 0.0F).uv(minU, minV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).uv(maxU, minV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).uv(maxU, maxV).endVertex();

            /* Fluid Name */
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            String name = stack.getDisplayName().getString();
            int nameWidth = fontRendererIn.width(name) / 2;
            //fontRendererIn.draw(matrixStack, name, -nameWidth, -14, -1);

            matrixStack.popPose();
        }
    }
}
