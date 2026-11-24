package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.client.screen.toolbar.IToolbarLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class IconButton extends Button implements IToolbarLabel
{
    private IconProvider icon;
    private Component label;

    public IconButton(int width, int height, @Nullable IconProvider icon, Component label, Button.OnPress onPress)
    {
        super(0, 0, width, height, Component.empty(), onPress, Supplier::get);
        this.icon = icon;
        this.label = label;
    }

    public IconButton setLabel(Component label)
    {
        this.label = label;
        return this;
    }

    @Override
    public Component getLabel()
    {
        return this.label;
    }

    public IconButton setIcon(@Nullable IconProvider icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int combinedWidth = this.icon != null ? 10 : 0;
        Component message = this.getMessage();
        if(!message.equals(Component.empty()))
        {
            combinedWidth += font.width(message);
            if(this.icon != null)
            {
                combinedWidth += 4;
            }
        }
        if(this.icon != null)
        {
            RenderSystem.setShaderTexture(0, this.icon.getTextureLocation());
            this.drawIcon(this.getX() + this.width / 2 - combinedWidth / 2, this.getY() + 5, this.icon.getU(), this.icon.getV());
        }

        if(!message.equals(Component.empty()))
        {
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX() + 12, this.getY(), 0xFFFFFF);
        }
    }

    protected void drawIcon(int x, int y, int u, int v)
    {
        int size = 10;
        float uScale = 1.0F / 100.0F;
        float vScale = 1.0F / 100.0F;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x, y + size, 0).uv(u * uScale, (v + size) * vScale).endVertex();
        buffer.vertex(x + size, y + size, 0).uv((u + size) * uScale, (v + size) * vScale).endVertex();
        buffer.vertex(x + size, y, 0).uv((u + size) * uScale, v * vScale).endVertex();
        buffer.vertex(x, y, 0).uv(u * uScale, v * vScale).endVertex();
        tessellator.end();
    }

    public interface IconProvider
    {
        ResourceLocation getTextureLocation();

        int getU();

        int getV();
    }
}
