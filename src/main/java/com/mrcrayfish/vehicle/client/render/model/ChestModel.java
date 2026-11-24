package com.mrcrayfish.vehicle.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;

/**
 * Author: MrCrayfish
 */
public class ChestModel
{
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private boolean christmas;

    public ChestModel()
    {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26)
        {
            this.christmas = true;
        }

        ModelPart modelpart = ChestRenderer.createSingleBodyLayer().bakeRoot();
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Pair<Float, Float> lidProgressPair, int light, float partialTicks)
    {
        float lidProgress = Mth.lerp(partialTicks, lidProgressPair.getLeft(), lidProgressPair.getRight());
        lidProgress = 1.0F - lidProgress;
        lidProgress = 1.0F - lidProgress * lidProgress * lidProgress;
        Material renderMaterial = this.christmas ? Sheets.CHEST_XMAS_LOCATION : Sheets.CHEST_LOCATION;
        VertexConsumer builder = renderMaterial.buffer(renderTypeBuffer, RenderType::entityCutout);
        this.renderChest(matrixStack, builder, lidProgress, light);
    }

    private void renderChest(PoseStack matrixStack, VertexConsumer builder, float openProgress, int lightTexture)
    {
        this.lid.xRot = -(openProgress * ((float) Math.PI / 2F));
        this.lock.xRot = this.lid.xRot;
        this.lid.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
        this.lock.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
        this.bottom.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
    }
}
