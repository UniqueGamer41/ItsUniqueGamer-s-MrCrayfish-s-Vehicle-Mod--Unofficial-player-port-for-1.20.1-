package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.PlaneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class SportsPlaneEntity extends PlaneEntity
{
    public SportsPlaneEntity(EntityType<? extends SportsPlaneEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(25.2F);
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling()
    {
        return this.getBoundingBox().inflate(1.5);
    }
}
