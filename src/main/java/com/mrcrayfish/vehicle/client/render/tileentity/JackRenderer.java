package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.block.entity.JackBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class JackRenderer implements BlockEntityRenderer<JackBlockEntity>
{
    private final BlockRenderDispatcher dispatcher;
    private final ModelBlockRenderer modelBlockRenderer;

    public JackRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.dispatcher = ctx.getBlockRenderDispatcher();
        this.modelBlockRenderer = ctx.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(JackBlockEntity entity, float delta, @NotNull PoseStack matrices, @NotNull MultiBufferSource buffers, int light, int overlay)
    {
        if(!entity.hasLevel())
            return;

        matrices.pushPose();
        {
            BlockPos pos = entity.getBlockPos();
            Level level = entity.getLevel();
            RandomSource random = level.getRandom();

            RenderType type = RenderType.cutout();
            BlockState state = entity.getLevel().getBlockState(pos);

            matrices.pushPose();
            {
                matrices.translate(0.5, 0.0, 0.5);
                matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(180F));
                matrices.translate(-0.5, 0.0, -0.5);
                BakedModel model = this.dispatcher.getBlockModel(state);
                VertexConsumer builder = buffers.getBuffer(type);

                this.modelBlockRenderer.tesselateBlock(level, model, state, pos, matrices, builder, true,
                        random, state.getSeed(pos), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
            }
            matrices.popPose();

            matrices.pushPose();
            {
                float progress = (entity.prevLiftProgress + (entity.liftProgress - entity.prevLiftProgress) * delta) / (float) JackBlockEntity.MAX_LIFT_PROGRESS;
                matrices.translate(0, 0.5 * progress, 0);

                //Render the head
                BlockState defaultState = ModBlocks.JACK_HEAD.get().defaultBlockState();
                BakedModel model = dispatcher.getBlockModel(ModBlocks.JACK_HEAD.get().defaultBlockState());
                VertexConsumer builder = buffers.getBuffer(RenderType.cutout());

                this.modelBlockRenderer.tesselateBlock(level, model, state, pos, matrices, builder, false,
                        random, 0L, overlay, ModelData.EMPTY, type);
            }
            matrices.popPose();

            matrices.pushPose();
            {
                Entity jackEntity = entity.getJack();
                if(jackEntity != null && jackEntity.getPassengers().size() > 0)
                {
                    Entity passenger = jackEntity.getPassengers().get(0);
                    if(passenger instanceof VehicleEntity vehicle && passenger.isAlive())
                    {
                        matrices.translate(0, 1 * 0.0625, 0);
                        matrices.translate(0.5, 0.5, 0.5);
                        float progress = (entity.prevLiftProgress + (entity.liftProgress - entity.prevLiftProgress) * delta) / (float) JackBlockEntity.MAX_LIFT_PROGRESS;
                        matrices.translate(0, 0.5 * progress, 0);

                        Vec3 heldOffset = vehicle.getProperties().getHeldOffset().yRot(passenger.getYRot() * 0.017453292F);
                        matrices.translate(-heldOffset.z * 0.0625, -heldOffset.y * 0.0625, -heldOffset.x * 0.0625);
                        matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(-passenger.getYRot()));

                        AbstractVehicleRenderer<VehicleEntity> wrapper = (AbstractVehicleRenderer<VehicleEntity>) VehicleRenderRegistry.getRenderer(vehicle.getType());
                        if(wrapper != null)
                        {
                            wrapper.setupTransformsAndRender(vehicle, matrices, buffers, delta, light);
                        }
                    }
                }
            }
            matrices.popPose();
        }
        matrices.popPose();
    }

    @Override
    public int getViewDistance()
    {
        return 65535;
    }
}
