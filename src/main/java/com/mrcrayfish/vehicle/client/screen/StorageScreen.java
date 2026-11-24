package com.mrcrayfish.vehicle.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class StorageScreen extends AbstractContainerScreen<StorageContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final Inventory playerInventory;
    private final int inventoryRows;

    public StorageScreen(StorageContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        //this.passEvents = false;
        this.inventoryRows = container.getStorageInventory().getContainerSize() / 9;
        this.imageHeight = 114 + this.inventoryRows * 18;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        graphics.drawString(minecraft.font, this.getTitle(), 8, 6, 4210752, false);
        graphics.drawString(minecraft.font, this.playerInventory.getDisplayName(), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        graphics.blit(CHEST_GUI_TEXTURE, startX, startY, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
        graphics.blit(CHEST_GUI_TEXTURE, startX, startY + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
