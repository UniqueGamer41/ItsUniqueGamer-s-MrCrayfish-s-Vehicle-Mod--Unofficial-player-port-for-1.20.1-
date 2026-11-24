package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.crafting.FluidEntry;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.block.entity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.client.render.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorScreen extends AbstractContainerScreen<FluidExtractorContainer>
{
    private static final ResourceLocation GUI = new ResourceLocation("vehicle:textures/gui/fluid_extractor.png");

    private final Inventory playerInventory;
    private final FluidExtractorBlockEntity fluidExtractorTileEntity;

    public FluidExtractorScreen(FluidExtractorContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.fluidExtractorTileEntity = container.getFluidExtractor();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.fluidExtractorTileEntity.getFluidStackTank() != null)
        {
            FluidStack stack = this.fluidExtractorTileEntity.getFluidStackTank();

            if(this.isMouseWithinRegion(startX + 127, startY + 14, 16, 59, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {
                    List<Component> tooltipText = Arrays.asList(
                            stack.getDisplayName(),
                            Component.literal(this.fluidExtractorTileEntity.getFluidLevel() + "/" + this.fluidExtractorTileEntity.getCapacity() + " mB")
                                    .withStyle(ChatFormatting.GRAY)
                    );
                   graphics.renderTooltip(Minecraft.getInstance().font, tooltipText, java.util.Optional.empty(), mouseX, mouseY);

                    //this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.literal(stack.getDisplayName().getString()), Component.literal(ChatFormatting.GRAY.toString() + this.fluidExtractorTileEntity.getFluidLevel() + "/" + this.fluidExtractorTileEntity.getCapacity() + " mB")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    graphics.renderTooltip(Minecraft.getInstance().font, Collections.singletonList(Component.literal("No Fluid")), java.util.Optional.empty(), mouseX, mouseY);
                    //this.renderTooltip(graphics, Lists.transform(Collections.singletonList(Component.literal("No Fluid")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY)
    {
        Font font = this.font;

        graphics.drawString(minecraft.font, this.title, 8, 6, 4210752, false);
        graphics.drawString(minecraft.font, this.playerInventory.getDisplayName(), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        this.renderBackground(graphics);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        RenderSystem.setShaderTexture(0, GUI);
        graphics.blit(GUI, startX, startY, 0, 0, this.imageWidth, this.imageHeight);

        if(this.fluidExtractorTileEntity.getRemainingFuel() >= 0)
        {
            int remainingFuel = (int) (14 * (this.fluidExtractorTileEntity.getRemainingFuel() / (double) this.fluidExtractorTileEntity.getFuelMaxProgress()));
            graphics.blit(GUI, startX + 64, startY + 53 + 14 - remainingFuel, 176, 14 - remainingFuel, 14, remainingFuel + 1);
        }

        if(this.fluidExtractorTileEntity.canExtract())
        {
            int left = startX + 93 + 1;
            int top = startY + 34;
            int right = left + 23 - 1;
            int bottom = top + 16;

            FluidEntry fluidExtract = this.fluidExtractorTileEntity.getCurrentRecipe().result();
            int fluidColor = -1;

            if(fluidExtract != null)
            {
                fluidColor = FluidUtils.getAverageFluidColor(fluidExtract.fluid());
            }

            RenderUtil.drawGradientRectHorizontal(left, top, right, bottom, -1, fluidColor);
            graphics.blit(GUI, startX + 93, startY + 34, 176, 14, 23, 16);

            double extractionPercentage = this.fluidExtractorTileEntity.getExtractionProgress() / (double) Config.SERVER.extractorExtractTime.get();
            int extractionProgress = (int) (22 * extractionPercentage + 1);
            graphics.blit(GUI, startX + 93 + extractionProgress, startY + 34, 93 + extractionProgress, 34, 23 - extractionProgress, 17);
        }

        this.drawFluidTank(this.fluidExtractorTileEntity.getFluidStackTank(), graphics, startX + 127, startY + 14, this.fluidExtractorTileEntity.getFluidLevel() / (double) this.fluidExtractorTileEntity.getCapacity(), 59);
    }

    private void drawFluidTank(FluidStack fluid, GuiGraphics graphics, int x, int y, double level, int height)
    {
        FluidUtils.drawFluidTankInGUI(fluid, x, y, level, height);
        RenderSystem.setShaderTexture(0, GUI);
        graphics.blit(GUI, x, y, 176, 44, 16, 59);
    }

    private boolean isMouseWithinRegion(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
