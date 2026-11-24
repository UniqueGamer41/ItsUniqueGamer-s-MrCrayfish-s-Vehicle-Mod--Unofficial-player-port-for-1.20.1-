package com.mrcrayfish.vehicle.client.screen.toolbar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.screen.DashboardScreen;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.IconButton;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.Spacer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractToolbarScreen extends Screen
{
    private static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/components.png");

    private Screen parent;
    private int contentWidth;

    protected AbstractToolbarScreen(Component titleIn, @Nullable Screen parent)
    {
        super(titleIn);
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        List<AbstractWidget> widgets = new ArrayList<>();
        if(this.parent != null)
        {
            widgets.add(new IconButton(20, 20, DashboardScreen.Icons.BACK, Component.translatable("vehicle.toolbar.label.back"), onPress -> this.minecraft.setScreen(this.parent)));
            widgets.add(Spacer.of(5));
        }
        this.loadWidgets(widgets);

        int contentWidth = (widgets.size() - 1) * 2 + 4;
        for(AbstractWidget widget : widgets)
        {
            contentWidth += widget.getWidth();
        }
        this.contentWidth = contentWidth;

        Pair<Integer, Integer> dimensions = this.getDimensionsForWindow(this.contentWidth, 24);
        int startX = (this.width - dimensions.getLeft()) / 2;
        int startY = (this.height - dimensions.getRight()) - dimensions.getRight() / 2;
        int offset = 0;

        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < widgets.size(); i++)
        {
            AbstractWidget widget = widgets.get(i);
            widget.setX(startX + 4 + 2 + offset);
            widget.setY(startY + 4 + 2);
            offset += widget.getWidth() + 2;
            this.addRenderableWidget(widget);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    protected abstract void loadWidgets(List<AbstractWidget> widgets);

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        graphics.fillGradient(0, this.height / 2, this.width, this.height, 0x00000000, 0xAA000000);

        Pair<Integer, Integer> dimensions = this.getDimensionsForWindow(this.contentWidth, 24);
        int startX = (this.width - dimensions.getLeft()) / 2;
        int startY = (this.height - dimensions.getRight()) - dimensions.getRight() / 2;
        this.drawWindow(startX, startY, dimensions);
        super.render(graphics, mouseX, mouseY, partialTicks);

        AbstractWidget hoveredWidget = null;
        for(Renderable widget : this.renderables)
        {
            if(!(widget instanceof AbstractWidget))
                break;

            AbstractWidget abstractWidget = (AbstractWidget) widget;

            if(CommonUtils.isMouseWithin(mouseX, mouseY, abstractWidget.getX(), abstractWidget.getY(), abstractWidget.getWidth(), abstractWidget.getHeight()))
            {
                hoveredWidget = abstractWidget;
                break;
            }
        }

        if(hoveredWidget instanceof IToolbarLabel)
        {
            Component message = ((IToolbarLabel) hoveredWidget).getLabel();
            int messageWidth = this.minecraft.font.width(message);
            graphics.drawString(this.minecraft.font, message, this.width / 2 - messageWidth / 2, startY - 12, 0xFFFFFF);
        }
    }

    public void drawWindow(int x, int y, Pair<Integer, Integer> dimensions)
    {
        this.drawWindow(x, y, dimensions.getLeft(), dimensions.getRight());
    }

    private void drawWindow(int x, int y, int width, int height)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        int offset = 17;
        this.drawTexturedRect(x, y, offset, 0, 4, 4, 4, 4);                              /* Top left corner */
        this.drawTexturedRect(x + width - 4, y, 5 + offset, 0, 4, 4, 4, 4);              /* Top right corner */
        this.drawTexturedRect(x, y + height - 4, offset, 5, 4, 4, 4, 4);                 /* Bottom left corner */
        this.drawTexturedRect(x + width - 4, y + height - 4, 5 + offset, 5, 4, 4, 4, 4); /* Bottom right corner */
        this.drawTexturedRect(x + 4, y, 4 + offset, 0, width - 8, 4, 1, 4);              /* Top border */
        this.drawTexturedRect(x + 4, y + height - 4, 4 + offset, 5, width - 8, 4, 1, 4); /* Bottom border */
        this.drawTexturedRect(x, y + 4, offset, 4, 4, height - 8, 4, 1);                 /* Left border */
        this.drawTexturedRect(x + width - 4, y + 4, 5 + offset, 4, 4, height - 8, 4, 1); /* Right border */
        this.drawTexturedRect(x + 4, y + 4, 4 + offset, 4, width - 8, height - 8, 1, 1); /* Center */
    }

    private void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        float uScale = 1.0F / 256.0F;
        float vScale = 1.0F / 256.0F;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(x, y + height, 0).uv(u * uScale, (v + textureHeight) * vScale).endVertex();
        builder.vertex(x + width, y + height, 0).uv((u + textureWidth) * uScale, (v + textureHeight) * vScale).endVertex();
        builder.vertex(x + width, y, 0).uv((u + textureWidth) * uScale, v * vScale).endVertex();
        builder.vertex(x, y, 0).uv(u * uScale, v * vScale).endVertex();
        tessellator.end();
    }

    private Pair<Integer, Integer> getDimensionsForWindow(int contentWidth, int contentHeight)
    {
        return Pair.of(contentWidth + 8, contentHeight + 8);
    }
}
