package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class GolfCartEntity extends LandVehicleEntity
{
    public GolfCartEntity(EntityType<? extends GolfCartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(6.4F);
        this.setMaxTurnAngle(45);
        this.setTurnSensitivity(6);

    }

}
