package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class SmartCarEntity extends LandVehicleEntity
{
    public SmartCarEntity(EntityType<? extends SmartCarEntity> type, Level worldIn)
    {
        super(type, worldIn);
        setMaxSpeed(10.4F);
        setMaxTurnAngle(45);
        setTurnSensitivity(12);
        this.canTowTrailers();
        //System.out.println("The max speed is" + getMaxSpeed());



    }


}
