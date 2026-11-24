package com.mrcrayfish.vehicle.entity.trailer;

import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import com.mrcrayfish.vehicle.network.message.MessageEntityFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Author: MrCrayfish
 */
public class FluidTrailerEntity extends TrailerEntity implements IEntityAdditionalSpawnData
{
    protected FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME * 100)
    {
        @Override
        protected void onContentsChanged()
        {
            syncTank();
        }
    };

    public FluidTrailerEntity(EntityType<? extends FluidTrailerEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }

//    @Override
//    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
//    {
//        if(!this.level.isClientSide && !player.isCrouching())
//        {
//            if(FluidUtil.interactWithFluidHandler(player, hand, tank))
//            {
//                return InteractionResult.SUCCESS;
//            }
//        }
//        return super.interact(player, hand);
//    }

//    @Override
//    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
//        if (!this.level().isClientSide && !player.isCrouching()) {
//            if (FluidUtil.interactWithFluidHandler(player, hand, tank)) {
//                return InteractionResult.SUCCESS;
//            }
//        }
//        return super.interact(player, hand);
//    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
    {
        if(!this.level().isClientSide && !player.isCrouching())
        {
            int before = this.tank.getFluidAmount();

            boolean interacted = FluidUtil.interactWithFluidHandler(player, hand, tank);

            int after = this.tank.getFluidAmount();

            // Only succeed if fluid amount actually changed
            if(interacted && before != after)
            {
                return InteractionResult.SUCCESS;
            }

            // Prevent endless use animation when full
            return InteractionResult.PASS;
        }

        return super.interact(player, hand);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if(compound.contains("Tank", Tag.TAG_COMPOUND))
        {
            this.tank.readFromNBT(compound.getCompound("Tank"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        CompoundTag tankTag = new CompoundTag();
        this.tank.writeToNBT(tankTag);
        compound.put("Tank", tankTag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> this.tank).cast();
        return super.getCapability(cap);
    }

    public FluidTank getTank()
    {
        return this.tank;
    }

    public void syncTank() {
        if (!this.level().isClientSide) {
            PacketHandler.getPlayChannel().sendToTrackingEntity(
                    () -> this,
                    new MessageEntityFluid(this.getId(), this.tank.getFluid())
            );
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        super.writeSpawnData(buffer);
        buffer.writeNbt(this.tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        super.readSpawnData(buffer);
        this.tank.readFromNBT(buffer.readNbt());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        EntityRayTracer.instance().registerInteractionBox(ModEntities.FLUID_TRAILER.get(), () -> {
            return createScaledBoundingBox(-7.0, -0.5, 12.0, 7.0, 3.5, 24.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
