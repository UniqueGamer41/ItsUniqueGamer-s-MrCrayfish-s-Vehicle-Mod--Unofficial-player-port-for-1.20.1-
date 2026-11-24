package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class BumperCarEntity extends LandVehicleEntity
{
    public BumperCarEntity(EntityType<? extends BumperCarEntity> type, Level worldIn)
    {
        super(type, worldIn);
        //TODO figure out fuel system
        this.setMaxSpeed(8.4F);
        this.setMaxTurnAngle(45);
        this.setTurnSensitivity(6);

    }

//    @Override
//    public void push(Entity entityIn)
//    {
////        if(entityIn instanceof BumperCarEntity && this.isVehicle())
////        {
////            applyBumperCollision((BumperCarEntity) entityIn);
////        }
//    }

//    private void applyBumperCollision(BumperCarEntity entity)
//    {
//        this.setDeltaMovement(this.getDeltaMovement().add(this.vehicleMotionX * 2, 0, this.vehicleMotionZ * 2));
////        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_BUMPER_CAR_BONK.get(), SoundCategory.NEUTRAL, 1.0F, 0.6F + 0.1F * this.getNormalSpeed());
//        this.currentSpeed *= 0.25F;
//    }

}