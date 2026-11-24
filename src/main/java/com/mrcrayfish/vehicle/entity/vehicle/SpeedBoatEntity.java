package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.BoatEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class SpeedBoatEntity extends BoatEntity
{
    public SpeedBoatEntity(EntityType<? extends SpeedBoatEntity> type, Level level)
    {
        super(type, level);
        this.setMaxSpeed(15F);
    }

    @Override
    public void tick()
    {
        super.tick();

        // 1. Update vehicle state BEFORE motion

        // 2. Apply buoyancy logic
        if (this.isInWater() && this.getFluidHeight() < this.getBbHeight() * 0.5F)
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D, 0.0D));
        }
        else if (this.isInWater())
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        }

        // 3. Apply motion update
        this.updateVehicleMotion();
    }

//    @Override
//    public void onUpdateVehicle()
//    {
//        // Handle player input
//        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player)
//        {
//            // Reset speed
//            this.currentSpeed = 0.0F;
//
//            // Forward
//            if (player.zza > 0)
//            {
//                this.currentSpeed += this.getAcceleration();
//            }
//
//            //forward and right
//            if (player.zza > 0 && player.xxa < 0)
//            {
//
//                this.currentSpeed += this.getAcceleration();
//                this.setYRot(this.getYRot() + this.getTurnSensitivity());
//            }
//
//            //forward and left
//            if(player.zza > 0 && player.xxa > 0){
//
//                this.currentSpeed += this.getAcceleration();
//                this.setYRot(this.getYRot() - this.getTurnSensitivity());
//            }
//
//            //reverse
//            if (player.zza < 0 )
//            {
//
//                this.currentSpeed -= this.getRevAcceleration();
//            }
//
//            //reverse and right
//
//            if (player.zza < 0 && player.xxa < 0)
//            {
//
//                this.currentSpeed -= this.getRevAcceleration();
//                this.setYRot(this.getYRot() + this.getTurnSensitivity());
//            }
//
//            //reverse and left
//            if (player.zza < 0 && player.xxa > 0)
//            {
//
//                this.currentSpeed -= this.getRevAcceleration();
//                this.setYRot(this.getYRot() - this.getTurnSensitivity());
//            }
//
//            // Turning without acceleration
//            if (player.xxa < 0)
//            {
//                this.setYRot(this.getYRot() + this.getTurnSensitivity());
//            }
//            if (player.xxa > 0)
//            {
//                this.setYRot(this.getYRot() - this.getTurnSensitivity());
//            }
//
//            // 🆕 Apply movement directionally based on current yaw and speed
//            float yawRad = (float) Math.toRadians(this.getYRot());
//            double motionX = -Math.sin(yawRad) * this.currentSpeed;
//            double motionZ = Math.cos(yawRad) * this.currentSpeed;
//
//            // Add to motion
//            this.setDeltaMovement(this.getDeltaMovement().add(motionX, 0.0D, motionZ));
//        }
//
//        super.onUpdateVehicle(); // Optional: call BoatEntity logic (e.g., bobbing)
//    }


    @Override
    public void createParticles()
    {
        if (this.state == State.IN_WATER && this.getThrottle() > 0)
        {
            for (int i = 0; i < 5; i++)
            {
                this.level().addParticle(ParticleTypes.SPLASH,
                        this.getX() + (this.random.nextFloat() - 0.5D) * this.getBbWidth(),
                        this.getBoundingBox().minY + 0.1D,
                        this.getZ() + (this.random.nextFloat() - 0.5D) * this.getBbWidth(),
                        -this.getDeltaMovement().x * 4.0D,
                        1.5D,
                        -this.getDeltaMovement().z * 4.0D);
            }

            for (int i = 0; i < 5; i++)
            {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextFloat() - 0.5D) * this.getBbWidth(),
                        this.getBoundingBox().minY + 0.1D,
                        this.getZ() + (this.random.nextFloat() - 0.5D) * this.getBbWidth(),
                        -this.getDeltaMovement().x * 2.0D,
                        0.0D,
                        -this.getDeltaMovement().z * 2.0D);
            }
        }
    }

    public boolean isInWater()
    {
        return this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER);
    }

    public double getFluidHeight()
    {
        return this.level().getFluidState(this.blockPosition()).getHeight(this.level(), this.blockPosition());
    }

    public float getAcceleration()
    {
        return 1.4F;
    }

    public float getRevAcceleration(){ return 0.2F; }

    public float getTurnSensitivity()
    {
        return 1.7F;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}