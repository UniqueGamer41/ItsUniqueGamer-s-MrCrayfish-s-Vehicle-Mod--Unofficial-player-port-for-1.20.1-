    package com.mrcrayfish.vehicle.client.screen;

    import com.google.common.collect.Lists;
    import com.mojang.blaze3d.platform.Lighting;
    import com.mojang.blaze3d.systems.RenderSystem;
    import com.mojang.blaze3d.vertex.*;
    import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
    import com.mrcrayfish.vehicle.client.render.Axis;
    import com.mrcrayfish.vehicle.client.render.CachedVehicle;
    import com.mrcrayfish.vehicle.common.entity.Transform;
    import com.mrcrayfish.vehicle.entity.EngineType;
    import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
    import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
    import com.mrcrayfish.vehicle.util.CommonUtils;
    import net.minecraft.ChatFormatting;
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
    import net.minecraft.client.renderer.MultiBufferSource;
    import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
    import net.minecraft.client.resources.language.I18n;
    import net.minecraft.network.chat.Component;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraft.world.Container;
    import net.minecraft.world.entity.player.Inventory;
    import org.jetbrains.annotations.NotNull;
    import org.joml.Quaternionf;
    import org.lwjgl.glfw.GLFW;
    import net.minecraft.client.gui.GuiGraphics;

    import java.util.Arrays;
    import java.util.Collections;

    /**
     * Author: MrCrayfish
     */
    public class EditVehicleScreen extends AbstractContainerScreen<EditVehicleContainer>
    {
        private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("vehicle:textures/gui/edit_vehicle.png");

        private final Inventory playerInventory;
        private final Container vehicleInventory;
        private final CachedVehicle cachedVehicle;

        private boolean showHelp = true;
        private int windowZoom = 10;
        private int windowX, windowY;
        private float windowRotationX, windowRotationY;
        private boolean mouseGrabbed;
        private int mouseGrabbedButton;
        private int mouseClickedX, mouseClickedY;

        public EditVehicleScreen(EditVehicleContainer container, Inventory playerInventory, Component title)
        {
            super(container, playerInventory, title);
            this.playerInventory = playerInventory;
            this.vehicleInventory = container.getVehicleInventory();
            this.cachedVehicle = new CachedVehicle(container.getVehicle());
            this.imageHeight = 184;
        }

        @Override
        protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
        {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GUI_TEXTURES);
            int left = (this.width - this.imageWidth) / 2;
            int top = (this.height - this.imageHeight) / 2;
            graphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

            if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
            {
                if(this.vehicleInventory.getItem(0).isEmpty())
                {
                    graphics.blit(GUI_TEXTURES, left + 8, top + 17, 176, 0, 16, 16);
                }
            }
            else if(this.vehicleInventory.getItem(0).isEmpty())
            {
                graphics.blit(GUI_TEXTURES,left + 8, top + 17, 176, 32, 16, 16);
            }

            if(this.cachedVehicle.getProperties().canChangeWheels())
            {
                if(this.vehicleInventory.getItem(1).isEmpty())
                {
                    graphics.blit( GUI_TEXTURES, left + 8, top + 35, 176, 16, 16, 16);
                }
            }
            else if(this.vehicleInventory.getItem(1).isEmpty())
            {
                graphics.blit(GUI_TEXTURES, left + 8, top + 35, 176, 32, 16, 16);
            }
        }

        @Override
        protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY)
        {
            Minecraft minecraft = Minecraft.getInstance();
            graphics.drawString(minecraft.font,this.title, 8, 6, 4210752, false);
            graphics.drawString(minecraft.font, this.playerInventory.getDisplayName(), 8, this.imageHeight - 96 + 2, 4210752, false);

            if(this.showHelp)
            {
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                {
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    graphics.drawString(minecraft.font, I18n.get("container.edit_vehicle.window_help"), 56, 38, 0xFFFFFF);
                }
                poseStack.popPose();
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})

        private void renderVehicleToBuffer(GuiGraphics graphics, PoseStack matrices, int mouseX, int mouseY, float partialTicks)
        {
            matrices.pushPose();
            {
                int left = (this.width - this.imageWidth) / 2;
                int top = (this.height - this.imageHeight) / 2;

                matrices.translate(left + 96, top + 78, 1050F);
                matrices.scale(-1F, -1F, -1F);
                RenderSystem.applyModelViewMatrix();
                graphics.enableScissor(
                        left + 26,
                        top + 17,
                        left + 168,
                        top + 87
                );

                Lighting.setupLevel(matrices.last().pose());

                PoseStack matrixStack = new PoseStack();
                AbstractVehicleRenderer renderer = this.cachedVehicle.getRenderer();
                if(renderer != null)
                {
                    matrixStack.pushPose();
                    {
                        matrixStack.translate(0.0D, 0.0D, 1000.0D);

//                        matrixStack.translate(0, -20, -150);
                        matrixStack.translate(
                                this.windowX -
                                        (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0),
                                0,
                                0
                        );
                        matrixStack.translate(0, this.windowY - (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);

                        Quaternionf quaternion = Axis.POSITIVE_X.rotationDegrees(-10F);
                        quaternion.mul(Axis.NEGATIVE_X.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
                        quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
                        quaternion.mul(
                                Axis.POSITIVE_Y.rotationDegrees(135F)
                        );
                        matrixStack.mulPose(quaternion);

                        matrixStack.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
                        matrixStack.scale(22F, 22F, 22F);

                        Transform position = this.cachedVehicle.getProperties().getDisplayTransform();
                        matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
                        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) position.getRotX()));
                        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) position.getRotY()));
                        matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) position.getRotZ()));
                        matrixStack.translate(position.getX(), position.getY(), position.getZ());

                        Lighting.setupForEntityInInventory();

                        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
                        renderManager.setRenderShadow(false);
                        renderManager.overrideCameraOrientation(quaternion);
                        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
                        RenderSystem.runAsFancy(() -> renderer.setupTransformsAndRender(this.menu.getVehicle(), matrixStack, renderTypeBuffer, partialTicks, 15728880));
                        renderTypeBuffer.endBatch();
                        renderManager.setRenderShadow(true);
                    }
                    matrixStack.popPose();
                }
            }
            graphics.disableScissor();
            matrices.popPose();

            RenderSystem.applyModelViewMatrix();

            Lighting.setupFor3DItems();
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
        {
            int startX = (this.width - this.imageWidth) / 2;
            int startY = (this.height - this.imageHeight) / 2;
            if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
            {
                if(scroll < 0 && this.windowZoom > 0)
                {
                    this.showHelp = false;
                    this.windowZoom--;
                }
                else if(scroll > 0)
                {
                    this.showHelp = false;
                    this.windowZoom++;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            int startX = (this.width - this.imageWidth) / 2;
            int startY = (this.height - this.imageHeight) / 2;

            if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
            {
                if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
                {
                    this.mouseGrabbed = true;
                    this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                    this.mouseClickedX = (int) mouseX;
                    this.mouseClickedY = (int) mouseY;
                    this.showHelp = false;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button)
        {
            if(this.mouseGrabbed)
            {
                if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                {
                    this.mouseGrabbed = false;
                    this.windowX -= (mouseX - this.mouseClickedX);
                    this.windowY -= (mouseY - this.mouseClickedY);
                }
                else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                {
                    this.mouseGrabbed = false;
                    this.windowRotationX += (mouseX - this.mouseClickedX);
                    this.windowRotationY -= (mouseY - this.mouseClickedY);
                }
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
        {
            this.renderBackground(graphics);
            super.render(graphics, mouseX, mouseY, partialTicks);
            this.renderVehicleToBuffer(graphics, RenderSystem.getModelViewStack(), mouseX, mouseY, partialTicks);

            this.renderTooltip(graphics, mouseX, mouseY);

            int startX = (this.width - this.imageWidth) / 2;
            int startY = (this.height - this.imageHeight) / 2;

            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 16, 18, 18))
                {
                    if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
                    {

                        graphics.renderTooltip(
                                minecraft.font, // 1. Pass the Font object
                                Collections.singletonList(Component.translatable("vehicle.tooltip.engine")),
                                java.util.Optional.empty(),// 2. Pass List<Component>
                                mouseX,
                                mouseY
                        );
                        //this.renderTooltip(graphics, Collections.singletonList(Component.translatable("vehicle.tooltip.engine")), mouseX, mouseY);
                        // this.renderTooltip(graphics,Lists.transform(Collections.singletonList(Component.translatable("vehicle.tooltip.engine")), Component::getVisualOrderText), mouseX, mouseY);
                    }
                    else
                    {

                       graphics.renderTooltip( minecraft.font, // 1. Pass the Font object
                               Arrays.asList(
                                       Component.translatable("vehicle.tooltip.engine"),
                                       Component.translatable("vehicle.tooltip.not_applicable").withStyle(ChatFormatting.GRAY)
                               ),
                               java.util.Optional.empty(),// 2. Pass List<Component>
                                mouseX,
                                mouseY);
                        //this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.translatable("vehicle.tooltip.engine"),  Component.translatable("vehicle.tooltip.not_applicable").withStyle(ChatFormatting.GRAY)), Component::getVisualOrderText), mouseX, mouseY);
                    }
                }
            }

            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 34, 18, 18))
                {
                    if(this.cachedVehicle.getProperties().canChangeWheels())
                    {

                        graphics.renderTooltip(
                                minecraft.font, // 1. Pass the Font object
                                Collections.singletonList(Component.translatable("vehicle.tooltip.wheels")),
                                java.util.Optional.empty(),// 2. Pass List<Component>
                                mouseX,
                                mouseY
                        );
                        // this.renderTooltip(matrices, Lists.transform(Collections.singletonList(Component.translatable("vehicle.tooltip.wheels")), Component::getVisualOrderText), mouseX, mouseY);
                    }
                    else
                    {

                        graphics.renderTooltip(
                                minecraft.font, // 1. Pass the Font object
                                Arrays.asList(
                                        Component.translatable("vehicle.tooltip.wheels"),
                                        Component.translatable("vehicle.tooltip.not_applicable").withStyle(ChatFormatting.GRAY)
                                ),
                                java.util.Optional.empty(),// 2. Pass List<Component>
                                mouseX,
                                mouseY
                        );
                        //this.renderTooltip(matrices, Lists.transform(Arrays.asList(Component.translatable("vehicle.tooltip.wheels"), Component.translatable("vehicle.tooltip.not_applicable").withStyle(ChatFormatting.GRAY)), Component::getVisualOrderText), mouseX, mouseY);
                    }
                }
            }
        }
    }
