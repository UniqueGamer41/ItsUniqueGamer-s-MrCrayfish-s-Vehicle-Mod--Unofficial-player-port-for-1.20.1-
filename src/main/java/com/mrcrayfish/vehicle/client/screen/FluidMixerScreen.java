package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.render.util.ColorHelper;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.inventory.container.FluidMixerContainer;
import com.mrcrayfish.vehicle.block.entity.FluidMixerBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.client.render.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class FluidMixerScreen extends AbstractContainerScreen<FluidMixerContainer>
{
    private static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/fluid_mixer.png");

    private final Inventory playerInventory;
    private final FluidMixerBlockEntity fluidMixerTileEntity;

    public FluidMixerScreen(FluidMixerContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.fluidMixerTileEntity = container.getFluidExtractor();
        this.imageWidth = 176;
        this.imageHeight = 180;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.fluidMixerTileEntity.getBlazeFluidStack() != null)
        {
            FluidStack stack = this.fluidMixerTileEntity.getBlazeFluidStack();
            if(this.isMouseWithinRegion(startX + 33, startY + 17, 16, 29, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {

                    List<Component> tooltipText = Arrays.asList(
                            stack.getDisplayName(), // FluidStack.getDisplayName() returns Component
                            Component.literal(this.fluidMixerTileEntity.getBlazeLevel() + "/" + this.fluidMixerTileEntity.getBlazeTank().getCapacity() + " mB")
                                    //.withStyle(ChatFormatting.GRAY) // Modern styling
                    );
                    // Explicit GuiGraphics renderTooltip call
                    assert minecraft != null;
                    //graphics.renderTooltip(minecraft.font, (List<? extends FormattedCharSequence>) tooltipText, mouseX, mouseY);
                    //this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.literal(stack.getDisplayName().getString()), Component.literal(ChatFormatting.GRAY.toString() + this.fluidMixerTileEntity.getBlazeLevel() + "/" + this.fluidMixerTileEntity.getBlazeTank().getCapacity() + " mB")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {

                    assert minecraft != null;
                    //graphics.renderTooltip(minecraft.font, (List<? extends FormattedCharSequence>) Collections.singletonList(Component.literal("No Fluid")), mouseX, mouseY);
                    //this.renderTooltip(matrices, Lists.transform(Collections.singletonList(Component.literal("No Fluid")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }

        if(this.fluidMixerTileEntity.getEnderSapFluidStack() != null)
        {
            FluidStack stack = this.fluidMixerTileEntity.getEnderSapFluidStack();
            if(this.isMouseWithinRegion(startX + 33, startY + 52, 16, 29, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {
                    List<Component> tooltipText = Arrays.asList(
                            stack.getDisplayName(),
                            Component.literal(this.fluidMixerTileEntity.getEnderSapLevel() + "/" + this.fluidMixerTileEntity.getEnderSapTank().getCapacity() + " mB")
                                    //.withStyle(ChatFormatting.GRAY)
                    );
                    //graphics.renderTooltip(minecraft.font, (List<? extends FormattedCharSequence>) tooltipText, mouseX, mouseY);

                    //this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.literal(stack.getDisplayName().getString()), Component.literal(ChatFormatting.GRAY.toString() + this.fluidMixerTileEntity.getEnderSapLevel() + "/" + this.fluidMixerTileEntity.getEnderSapTank().getCapacity() + " mB")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    //graphics.renderTooltip(minecraft.font, (List<? extends FormattedCharSequence>) Collections.singletonList(Component.literal("No Fluid")), mouseX, mouseY);
                    //this.renderTooltip(matrices, Lists.transform(Collections.singletonList(Component.literal("No Fluid")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }

        if(this.fluidMixerTileEntity.getFueliumFluidStack() != null)
        {
            FluidStack stack = this.fluidMixerTileEntity.getFueliumFluidStack();
            if(this.isMouseWithinRegion(startX + 151, startY + 20, 16, 59, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {
                    List<Component> tooltipText = Arrays.asList(
                            stack.getDisplayName(),
                            Component.literal(this.fluidMixerTileEntity.getFueliumLevel() + "/" + this.fluidMixerTileEntity.getFueliumTank().getCapacity() + " mB")
                                    //.withStyle(ChatFormatting.GRAY)
                    );
                    graphics.renderTooltip(minecraft.font, tooltipText, java.util.Optional.empty(), mouseX, mouseY);
                    // this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.literal(stack.getDisplayName().getString()), Component.literal(ChatFormatting.GRAY.toString() + this.fluidMixerTileEntity.getFueliumLevel() + "/" + this.fluidMixerTileEntity.getFueliumTank().getCapacity() + " mB")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    graphics.renderTooltip(minecraft.font, Collections.singletonList(Component.literal("No Fluid")), java.util.Optional.empty(), mouseX, mouseY);
                    //this.renderTooltip(matrices, Lists.transform(Collections.singletonList(Component.literal("No Fluid")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }

        this.renderTooltip(graphics, mouseX, mouseY);
        //this.renderTooltip(matrices, mouseX, mouseY);
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

        if(this.fluidMixerTileEntity.getRemainingFuel() >= 0)
        {
            int remainingFuel = (int) (14 * (this.fluidMixerTileEntity.getRemainingFuel() / (double) this.fluidMixerTileEntity.getFuelMaxProgress()));
            graphics.blit(GUI, startX + 9, startY + 31 + 14 - remainingFuel, 176, 14 - remainingFuel, 14, remainingFuel + 1);
        }

        if(this.fluidMixerTileEntity.canMix())
        {
            int blazeColor = FluidUtils.getAverageFluidColor(this.fluidMixerTileEntity.getBlazeFluidStack().getFluid());
            int sapColor = FluidUtils.getAverageFluidColor(this.fluidMixerTileEntity.getEnderSapFluidStack().getFluid());

            int redBlaze = ColorHelper.unpackARGBRed(blazeColor);
            int greenBlaze = ColorHelper.unpackARGBGreen(blazeColor);
            int blueBlaze = ColorHelper.unpackARGBBlue(blazeColor);

            int redSap = ColorHelper.unpackARGBRed(sapColor);
            int greenSap = ColorHelper.unpackARGBGreen(sapColor);
            int blueSap = ColorHelper.unpackARGBBlue(sapColor);

            int statrColor = ColorHelper.packARGB(((redBlaze + redSap) / 2), ((greenBlaze + greenSap) / 2), ((blueBlaze + blueSap) / 2), 130);
            int fluidColor = FluidUtils.getAverageFluidColor(ModFluids.FUELIUM.get()); //TODO change to recipe

            double extractionPercentage = this.fluidMixerTileEntity.getExtractionProgress() / (double) Config.SERVER.mixerMixTime.get();

            double lenghtItem = 76;
            double lenghtHorizontal = 12;
            double lenghtVerticle = 8;
            double lenghtNode = 10;
            double lenghtTotal = lenghtItem + lenghtHorizontal + lenghtVerticle + lenghtNode * 2;
            double percentageStart = 0;

            double percentageHorizontal = Mth.clamp((extractionPercentage - percentageStart) / (lenghtHorizontal / lenghtTotal), 0, 1);
            int left = startX + 51;
            int top = startY + 27;
            RenderUtil.drawGradientRectHorizontal(left, top, (int) (left + 12 * percentageHorizontal), top + 8, blazeColor, blazeColor);
            top += 36;
            RenderUtil.drawGradientRectHorizontal(left, top, (int) (left + 12 * percentageHorizontal), top + 8, sapColor, sapColor);
            percentageStart += lenghtHorizontal / lenghtTotal;

            left += 12;
            top -= 37;
            int colorFade;
            if (extractionPercentage >= percentageStart)
            {
                int alpha = (int) (130 * Mth.clamp((extractionPercentage - percentageStart) / (lenghtNode / lenghtTotal), 0, 1));
                colorFade = ColorHelper.repackAlpha(blazeColor, alpha);
                RenderUtil.drawGradientRectHorizontal(left, top, left + 10, top + 10, colorFade, colorFade);
                colorFade = ColorHelper.repackAlpha(sapColor, alpha);
                top += 36;
                RenderUtil.drawGradientRectHorizontal(left, top, left + 10, top + 10, colorFade, colorFade);
            }
            percentageStart += lenghtNode / lenghtTotal;

            left += 1;
            top -= 26;
            if (extractionPercentage >= percentageStart)
            {
                double percentageVerticle = Mth.clamp((extractionPercentage - percentageStart) / (lenghtVerticle / lenghtTotal), 0, 1);
                RenderUtil.drawGradientRectHorizontal(left, top, left + 8, (int) (top + 8 * percentageVerticle), blazeColor, blazeColor);
                top += 26;
                RenderUtil.drawGradientRectHorizontal(left, (int) (top - 8 * percentageVerticle), left + 8, top, sapColor, sapColor);
            }
            percentageStart += lenghtVerticle / lenghtTotal;

            left -= 1;
            top -= 18;
            if (extractionPercentage >= percentageStart)
            {
                int alpha = (int) (130 * Mth.clamp((extractionPercentage - percentageStart) / (lenghtNode / lenghtTotal), 0, 1));
                colorFade = ColorHelper.repackAlpha(statrColor, alpha);
                RenderUtil.drawGradientRectHorizontal(left, top, left + 10, top + 10, colorFade, colorFade);
            }
            percentageStart += lenghtNode / lenghtTotal;

            if (extractionPercentage >= percentageStart)
            {
                left = startX + 73;
                top = startY + 36;
                int right = left + 76;
                int bottom = top + 26;

                double percentageItem = Mth.clamp((extractionPercentage - percentageStart) / (lenghtItem / lenghtTotal), 0, 1);
                RenderUtil.drawGradientRectHorizontal(left, top, right, bottom, statrColor, fluidColor);
                graphics.blit(GUI, left, top, 176, 14, 76, 26);

                int extractionProgress = (int) (76 * percentageItem + 1);
                graphics.blit(GUI, left + extractionProgress, top, 73 + extractionProgress, 36, 76 - extractionProgress, 26);
            }
        }

        this.drawSmallFluidTank(this.fluidMixerTileEntity.getBlazeFluidStack(), graphics, startX + 33, startY + 17, this.fluidMixerTileEntity.getBlazeLevel() / (double) this.fluidMixerTileEntity.getBlazeTank().getCapacity());
        this.drawSmallFluidTank(this.fluidMixerTileEntity.getEnderSapFluidStack(), graphics, startX + 33, startY + 52, this.fluidMixerTileEntity.getEnderSapLevel() / (double) this.fluidMixerTileEntity.getEnderSapTank().getCapacity());
        this.drawFluidTank(this.fluidMixerTileEntity.getFueliumFluidStack(), graphics, startX + 151, startY + 20, this.fluidMixerTileEntity.getFueliumLevel() / (double) this.fluidMixerTileEntity.getFueliumTank().getCapacity());
    }

    private void drawFluidTank(FluidStack fluid, GuiGraphics graphics, int x, int y, double level)
    {
        FluidUtils.drawFluidTankInGUI(fluid, x, y, level, 59);
        RenderSystem.setShaderTexture(0, GUI);
        graphics.blit(GUI, x, y, 176, 44, 16, 59);
    }

    private void drawSmallFluidTank(FluidStack fluid, GuiGraphics graphics, int x, int y, double level)
    {
        FluidUtils.drawFluidTankInGUI(fluid, x, y, level, 29);
        RenderSystem.setShaderTexture(0, GUI);
        graphics.blit(GUI, x, y, 176, 44, 16, 29);
    }

    private boolean isMouseWithinRegion(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
