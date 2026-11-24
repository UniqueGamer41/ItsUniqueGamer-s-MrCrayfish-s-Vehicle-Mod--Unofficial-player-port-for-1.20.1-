package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.block.FluidPumpBlock;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.block.entity.PumpBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;


/**
 * Author: MrCrayfish
 */
public class FluidPumpRenderer implements BlockEntityRenderer<PumpBlockEntity>
{
    private final Camera camera;
    private final HitResult hitResult;
    private final Font font;

    public FluidPumpRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.camera = ctx.getBlockEntityRenderDispatcher().camera;
        this.hitResult = ctx.getBlockEntityRenderDispatcher().cameraHitResult;

        this.font = ctx.getFont();
    }

    @Override
    public void render(PumpBlockEntity entity, float delta, PoseStack matrices, MultiBufferSource buffers, int light, int overlay)
    {
        if(this.camera == null)
        {
            return;
        }

        Entity cameraEntity = this.camera.getEntity();
        if(!(cameraEntity instanceof Player player))
            return;

        if(player.getMainHandItem().getItem() != ModItems.WRENCH.get())
            return;

        this.renderInteractableBox(entity, matrices, buffers);

        if(this.hitResult == null)
            return;

        if(this.hitResult.getType() != HitResult.Type.BLOCK)
            return;

        BlockHitResult result = (BlockHitResult) this.hitResult;
        if(!result.getBlockPos().equals(entity.getBlockPos()))
            return;

        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();
        FluidPumpBlock fluidPumpBlock = (FluidPumpBlock) state.getBlock();
        if(!fluidPumpBlock.isLookingAtHousing(state, this.hitResult.getLocation().add(-pos.getX(), -pos.getY(), -pos.getZ())))
            return;

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);

        Direction direction = state.getValue(FluidPumpBlock.DIRECTION);
        matrices.translate(-direction.getStepX() * 0.35, -direction.getStepY() * 0.35, -direction.getStepZ() * 0.35);

        matrices.mulPose(this.camera.rotation());
        matrices.scale(-0.015F, -0.015F, 0.015F);
        Matrix4f matrix4f = matrices.last().pose();
        Font fontRenderer = this.font;
        Component text = Component.translatable(entity.getPowerMode().getKey());
        float x = (float)(-fontRenderer.width(text) / 2);
        //fontRenderer.drawInBatch(text, x, 0, 15728880, Style.EMPTY.withColor(TextColor.fromRgb(15728880)), matrix4f, buffers, false, 0, OverlayTexture.NO_OVERLAY);
        matrices.popPose();
    }

    private void renderInteractableBox(PumpBlockEntity tileEntity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer)
    {
        if(this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK)
        {
            BlockHitResult result = (BlockHitResult) this.hitResult;
            if(result.getBlockPos().equals(tileEntity.getBlockPos()))
            {
                BlockPos pos = tileEntity.getBlockPos();
                BlockState state = tileEntity.getBlockState();
                FluidPumpBlock fluidPumpBlock = (FluidPumpBlock) state.getBlock();
                if(fluidPumpBlock.isLookingAtHousing(state, this.hitResult.getLocation().add(-pos.getX(), -pos.getY(), -pos.getZ())))
                {
                    return;
                }
            }
        }

        BlockState state = tileEntity.getBlockState();
        VoxelShape shape = FluidPumpBlock.PUMP_BOX[state.getValue(FluidPumpBlock.DIRECTION).getOpposite().get3DDataValue()];
        VertexConsumer builder = renderTypeBuffer.getBuffer(RenderType.lines());
        EntityRayTracer.renderShape(matrixStack, builder, shape, 1.0F, 0.77F, 0.29F, 1.0F);
    }
}
