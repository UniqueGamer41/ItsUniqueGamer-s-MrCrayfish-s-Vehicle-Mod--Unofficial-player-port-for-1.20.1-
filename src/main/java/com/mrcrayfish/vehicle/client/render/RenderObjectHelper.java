package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.client.render.util.ColorHelper;
import com.mrcrayfish.vehicle.client.util.OptifineHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

/**
 * @author Mo0dss
 */
@OnlyIn(Dist.CLIENT)
public class RenderObjectHelper
{
    protected static final RandomSource RANDOM = new XoroshiroRandomSource(42L);
    private static final float[] EMPTY_COLOR = new float[] { 1F, 1F, 1F, 1F };
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    public static BakedModel getModel(ItemStack stack)
    {
        return MINECRAFT.getItemRenderer().getItemModelShaper().getItemModel(stack);
    }

    public static void renderColoredModel(BakedModel model, ItemDisplayContext displayContext, boolean leftHanded, PoseStack matrices, MultiBufferSource renderTypeBuffer, int color, int overlay, int light)
    {
        matrices.pushPose();
        {
            model = ForgeHooksClient.handleCameraTransforms(matrices, model, displayContext, leftHanded);
            matrices.translate(-0.5, -0.5, -0.5);

            if(!model.isCustomRenderer())
            {
                PoseStack.Pose pose = matrices.last();
                RenderType type = Sheets.cutoutBlockSheet();
                VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(type);

                renderModel(pose, vertexBuilder, type, ItemStack.EMPTY, model, color, overlay, light);
            }
        }
        matrices.popPose();
    }

    public static void renderDamagedVehicleModel(BakedModel model, ItemDisplayContext itemDisplayContext, boolean leftHanded, PoseStack matrices, int stage, int color, int overlay, int light)
    {
        matrices.pushPose();
        {
            model = ForgeHooksClient.handleCameraTransforms(matrices, model, itemDisplayContext, leftHanded);
            matrices.translate(-0.5, -0.5, -0.5);

            if(!model.isCustomRenderer())
            {
                PoseStack.Pose pose = matrices.last();
                RenderType type = ModelBakery.DESTROY_TYPES.get(stage);
                renderModel(pose, new SheetedDecalTextureGenerator(
                        MINECRAFT.renderBuffers().crumblingBufferSource().getBuffer(type),
                        pose.pose(), pose.normal(), light), type, ItemStack.EMPTY, model, color, overlay, light);
                //TODO maybe unsafe
            }
        }
        matrices.popPose();
    }

    public static void renderModel(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack matrices, MultiBufferSource renderTypeBuffer, int overlay, int light, BakedModel model)
    {
        if(!stack.isEmpty())
        {
            matrices.pushPose();
            {
                boolean isGui = displayContext == ItemDisplayContext.GUI;
                boolean tridentFlag = isGui || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED;

                if(stack.is(Items.TRIDENT) && tridentFlag)
                {
                    model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation("minecraft:trident"), "inventory"));
                }
                else if (stack.is(Items.SPYGLASS))
                {
                    model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation("minecraft:spyglass"), "inventory"));
                }

                model = ForgeHooksClient.handleCameraTransforms(matrices, model, displayContext, leftHanded);
                matrices.translate(-0.5, -0.5, -0.5);

                if(!model.isCustomRenderer() && (!stack.is(Items.TRIDENT) || tridentFlag))
                {
                    boolean fabulous = true;
                    if (!isGui && !displayContext.firstPerson() && stack.getItem() instanceof BlockItem)
                    {
                        Block block = ((BlockItem) stack.getItem()).getBlock();
                        fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                    }

                    PoseStack.Pose pose = matrices.last();
                    for (var subModel : model.getRenderPasses(stack, fabulous))
                    {
                        for (var subRenderType : subModel.getRenderTypes(stack, fabulous))
                        {
                            VertexConsumer buffer = fabulous ?
                                    ItemRenderer.getFoilBufferDirect(renderTypeBuffer, subRenderType, true, stack.hasFoil()) :
                                    ItemRenderer.getFoilBuffer(renderTypeBuffer, subRenderType, true, stack.hasFoil());

                            renderModel(pose, buffer, subRenderType, stack, subModel, -1, overlay, light);
                        }
                    }
                }
                else
                {
                    IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, displayContext, matrices, renderTypeBuffer, light, overlay);
                }
            }
            matrices.popPose();
        }
    }

    private static void renderModel(PoseStack.Pose pose, VertexConsumer consumer, RenderType type, ItemStack stack, BakedModel model, int color, int overlay, int light)
    {
        RandomSource random = RANDOM;
        int[] lights = new int[] { light, light, light, light };

        for(Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderQuadList(pose, consumer, model.getQuads(null, direction, random, ModelData.EMPTY, type), stack, color, overlay, lights);
        }

        random.setSeed(42L);
        renderQuadList(pose, consumer, model.getQuads(null, null, random, ModelData.EMPTY, type), stack, color, overlay, lights);
    }

    protected static void renderQuadList(PoseStack.Pose pose, VertexConsumer consumer, List<BakedQuad> quads, ItemStack stack, int color, int overlay, int[] light)
    {
        boolean useItemColor = !stack.isEmpty() && color == -1;

        // This is a very hot allocation, iterate over it manually
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++)
        {
            BakedQuad quad = quads.get(i);
            int tintColor = 0xFFFFFFFF;

            if(OptifineHelper.isEmissiveTexturesEnabled())
            {
                quad = OptifineHelper.castAsEmissive(quad);

                if (quad == null)
                {
                    continue;
                }
            }

            if(quad.isTinted())
            {
                if(useItemColor)
                {
                    tintColor = MINECRAFT.getItemColors().getColor(stack, quad.getTintIndex());
                }
                else
                {
                    tintColor = color;
                }

                if (OptifineHelper.isCustomColorsEnabled())
                {
                    tintColor = OptifineHelper.castAsCustomColor(stack, quad.getTintIndex(), tintColor);
                }
            }

            float red =  ColorHelper.normalize(ColorHelper.unpackARGBRed(tintColor));
            float green = ColorHelper.normalize(ColorHelper.unpackARGBGreen(tintColor));
            float blue = ColorHelper.normalize(ColorHelper.unpackARGBBlue(tintColor));
            float alpha = ColorHelper.normalize(ColorHelper.unpackARGBAlpha(tintColor));

            consumer.putBulkData(pose, quad, EMPTY_COLOR, red, green, blue, alpha, light, overlay, true);
        }
    }
}
