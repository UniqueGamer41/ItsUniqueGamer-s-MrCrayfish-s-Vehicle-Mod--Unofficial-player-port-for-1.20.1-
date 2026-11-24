package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class SofacopterEntity extends HelicopterEntity
{
    public SofacopterEntity(EntityType<? extends SofacopterEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.entityData.set(COLOR, 11546150);
    }

}
