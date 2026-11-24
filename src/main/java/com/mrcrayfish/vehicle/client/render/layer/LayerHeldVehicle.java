package com.mrcrayfish.vehicle.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.handler.HeldVehicleHandler;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class LayerHeldVehicle<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends RenderLayer<T, M>
{
    private VehicleEntity vehicle;
    private CachedVehicle cachedVehicle;
    private float width = -1.0F;

    public LayerHeldVehicle(RenderLayerParent<T, M> renderer)
    {
        super(renderer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(@NotNull PoseStack matrices, @NotNull MultiBufferSource buffers, int light, @NotNull Player player, float v, float v1, float delta, float v3, float v4, float v5)
    {
        CompoundTag tagCompound = HeldVehicleDataHandler.getHeldVehicle(player);
        if(!tagCompound.isEmpty())
        {
            if(this.cachedVehicle == null)
            {
                Optional<EntityType<?>> optional = EntityType.byString(tagCompound.getString("id"));
                if(optional.isPresent())
                {
                    EntityType<?> entityType = optional.get();
                    Entity entity = entityType.create(player.level());

                    if(entity instanceof VehicleEntity)
                    {
                        entity.load(tagCompound);
                        this.vehicle = (VehicleEntity) entity;
                        this.width = entity.getBbWidth();
                        this.cachedVehicle = new CachedVehicle(this.vehicle);
                    }
                }
            }

            if(this.cachedVehicle != null)
            {
                matrices.pushPose();
                {
                    HeldVehicleHandler.AnimationCounter counter = HeldVehicleHandler.idToCounter.get(player.getUUID());
                    if(counter != null)
                    {
                        float width = this.width / 2;
                        matrices.translate(0F, 1F - counter.getProgress(delta), -0.5F * Math.sin(Math.PI * counter.getProgress(delta)) - width * (1.0F - counter.getProgress(delta)));
                    }

                    Vec3 heldOffset = this.cachedVehicle.getProperties().getHeldOffset();

                    matrices.translate(heldOffset.x * 0.0625D, heldOffset.y * 0.0625D, heldOffset.z * 0.0625D);
                    matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(180F));
                    matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(-90F));
                    matrices.translate(0F, player.isCrouching() ? 0.3125F : 0.5625F, 0F);

                    ((AbstractVehicleRenderer<VehicleEntity>) this.cachedVehicle.getRenderer()).setupTransformsAndRender(this.vehicle, matrices, buffers, delta, light);
                }
                matrices.popPose();
            }
        }
        else
        {
            this.cachedVehicle = null;
            this.width = -1.0F;
        }
    }
}
