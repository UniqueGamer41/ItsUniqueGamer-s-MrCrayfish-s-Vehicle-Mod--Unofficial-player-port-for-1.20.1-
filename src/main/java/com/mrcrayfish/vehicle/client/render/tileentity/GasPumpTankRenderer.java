package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.RotatedObjectBlock;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.block.entity.GasPumpTankBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankRenderer implements BlockEntityRenderer<GasPumpTankBlockEntity>
{
    private static final FluidUtils.FluidSides FLUID_SIDES = new FluidUtils.FluidSides(Direction.NORTH, Direction.SOUTH, Direction.UP);

    public GasPumpTankRenderer(BlockEntityRendererProvider.Context ctx)
    {

    }

    @Override
    public void render(GasPumpTankBlockEntity entity, float delta, @NotNull PoseStack matrices, @NotNull MultiBufferSource buffers, int light, int overlay)
    {
        Level world = entity.getLevel();
        BlockState state = entity.getBlockState();
        if(state.getBlock() != ModBlocks.GAS_PUMP.get())
            return;

        FluidTank tank = entity.getFluidTank();
        if(tank.isEmpty())
            return;

        matrices.pushPose();
        {
            Direction direction = state.getValue(RotatedObjectBlock.DIRECTION);
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(direction.get2DDataValue() * -90F - 90F));
            matrices.translate(-0.5, -0.5, -0.5);
            float height = 11.0F * (tank.getFluidAmount() / (float) tank.getCapacity());
            FluidUtils.drawFluidInWorld(tank, world, entity.getBlockPos(), matrices, buffers, 2.01F * 0.0625F, 4F * 0.0625F, 5F * 0.0625F, (12 - 0.02F) * 0.0625F, height * 0.0625F, 6F * 0.0625F, light, FLUID_SIDES);
        }
        matrices.popPose();
    }
}
