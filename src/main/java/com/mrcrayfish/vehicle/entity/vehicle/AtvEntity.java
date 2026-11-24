package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AtvEntity extends LandVehicleEntity
{
    public AtvEntity(EntityType<? extends AtvEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setMaxSpeed(17);
        this.setMaxTurnAngle(20);
        this.setTurnSensitivity(5);
    }

//    @Override
//    public SoundEvent getEngineSound()
//    {
//        return ModSounds.ENTITY_ATV_ENGINE.get();
//    }




}
