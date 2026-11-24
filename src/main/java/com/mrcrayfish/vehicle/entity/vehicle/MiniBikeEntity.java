package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.MotorcycleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class MiniBikeEntity extends MotorcycleEntity
{
    public MiniBikeEntity(EntityType<? extends MiniBikeEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(12.4F);
        this.setMaxTurnAngle(45);
        this.setTurnSensitivity(12);
    }
}
