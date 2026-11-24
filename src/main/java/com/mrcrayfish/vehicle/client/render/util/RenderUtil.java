package com.mrcrayfish.vehicle.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.util.port.Vector4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.BlockAndTintGetter;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class RenderUtil
{
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors (ARGB format).
     */
    public static void drawGradientRectHorizontal(int left, int top, int right, int bottom, int leftColor, int rightColor)
    {
        //RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        int rightRed = ColorHelper.unpackARGBRed(rightColor);
        int rightGreen = ColorHelper.unpackARGBGreen(rightColor);
        int rightBlue = ColorHelper.unpackARGBBlue(rightColor);
        int rightAlpha = ColorHelper.unpackARGBAlpha(rightColor);

        int leftRed = ColorHelper.unpackARGBRed(leftColor);
        int leftGreen = ColorHelper.unpackARGBGreen(leftColor);
        int leftBlue = ColorHelper.unpackARGBBlue(leftColor);
        int leftAlpha = ColorHelper.unpackARGBAlpha(leftColor);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(right, top, 0).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
        bufferbuilder.vertex(left, top, 0).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
        bufferbuilder.vertex(left, bottom, 0).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
        bufferbuilder.vertex(right, bottom, 0).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
        //RenderSystem.enableTexture();
    }

    public static void scissor(int x, int y, int width, int height) //TODO might need fixing. I believe I rewrote this in a another mod
    {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(x * scale, mc.getWindow().getScreenHeight() - y * scale - height * scale, Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static int getTransformedLight(Matrix4f pos, BlockAndTintGetter level, BlockPos.MutableBlockPos mpos, float x, float y, float z)
    {
        Vector4f vec = new Vector4f(x, y, z, 1F);
        vec.transform(pos);

        return LevelRenderer.getLightColor(level, mpos.set(vec.x(), vec.y(), vec.z()));
    }

    public static List<Component> lines(FormattedText text, int maxWidth)
    {
        List<FormattedText> lines = MINECRAFT.font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);
        return lines.stream().map(t -> Component.literal(t.getString()).withStyle(ChatFormatting.GRAY)).collect(Collectors.toList());
    }
}
