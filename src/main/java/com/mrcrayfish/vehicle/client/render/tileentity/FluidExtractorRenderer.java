package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.FluidExtractorBlock;
import com.mrcrayfish.vehicle.block.entity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRenderer implements BlockEntityRenderer<FluidExtractorBlockEntity>
{
    private static final FluidUtils.FluidSides FLUID_SIDES = new FluidUtils.FluidSides(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.UP);

    public FluidExtractorRenderer(BlockEntityRendererProvider.Context ctx)
    {}

    @Override
    public void render(FluidExtractorBlockEntity entity, float delta, PoseStack matrices, MultiBufferSource buffers, int light, int overlay)
    {
        FluidTank tank = entity.getFluidTank();
        if(tank.isEmpty())
            return;

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        Direction direction = entity.getBlockState().getValue(FluidExtractorBlock.DIRECTION);
        matrices.mulPose(Vector3f.YP.rotationDegrees(direction.get2DDataValue() * -90F - 90F));
        matrices.translate(-0.5, -0.5, -0.5);
        float height = 12.0F * tank.getFluidAmount() / (float) tank.getCapacity();
        FluidUtils.drawFluidInWorld(tank, entity.getLevel(), entity.getBlockPos(), matrices, buffers, 9F * 0.0625F, 2F * 0.0625F, 0.01F * 0.0625F, 6.99F * 0.0625F, height * 0.0625F, (16 - 0.02F) * 0.0625F, light, FLUID_SIDES);
        matrices.popPose();
    }
}
