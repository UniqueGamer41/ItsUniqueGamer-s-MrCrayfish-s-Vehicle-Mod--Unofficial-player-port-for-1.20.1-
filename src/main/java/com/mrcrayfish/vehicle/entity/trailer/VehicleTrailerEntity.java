package com.mrcrayfish.vehicle.entity.trailer;

import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class VehicleTrailerEntity extends TrailerEntity
{
    public VehicleTrailerEntity(EntityType<? extends VehicleTrailerEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public double getPassengersRidingOffset()
    {
        return 8 * 0.0625;
    }

    @Override
    protected boolean canRide(@NotNull Entity entityIn)
    {
        return true;
    }

//    @Override
//    public void positionRider(@NotNull Entity passenger)
//    {
//        if(passenger instanceof VehicleEntity)
//        {
//            Vec3 offset = ((VehicleEntity) passenger).getProperties().getTrailerOffset().yRot((float) Math.toRadians(-this.getYRot()));
//            passenger.setPos(this.getX() + offset.x, this.getY() + getPassengersRidingOffset() + offset.y, this.getZ() + offset.z);
//            passenger.yRotO = this.yRotO;
//            passenger.setYRot(this.getYRot());
//        }
//    }


    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull Entity.MoveFunction moveFunction)
    {
        if(passenger instanceof VehicleEntity)
        {
            // 1. Calculate the final position (Vec3 is recommended for position functions)
            Vec3 offset = ((VehicleEntity) passenger).getProperties().getTrailerOffset()
                    .yRot((float) Math.toRadians(-this.getYRot()));

            double x = this.getX() + offset.x;
            double y = this.getY() + this.getPassengersRidingOffset() + offset.y;
            double z = this.getZ() + offset.z;

            // 2. Call the provided move function to actually set the position
            // This is the key difference in 1.20.1+
            moveFunction.accept(passenger, x, y, z);

            // 3. Set rotations (these calls are usually still correct)
            passenger.yRotO = this.yRotO;
            passenger.setYRot(this.getYRot());
        }
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger)
    {
        return passenger instanceof VehicleEntity && this.getPassengers().size() == 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        EntityRayTracer.instance().registerInteractionBox(ModEntities.VEHICLE_TRAILER.get(), () -> {
            return createScaledBoundingBox(-7.0, -0.5, 14.0, 7.0, 3.5, 24.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
