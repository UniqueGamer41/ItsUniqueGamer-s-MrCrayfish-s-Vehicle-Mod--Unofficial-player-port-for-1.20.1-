package com.mrcrayfish.vehicle.client.render;

import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractTrailerRenderer<T extends TrailerEntity> extends AbstractVehicleRenderer<T>
{
    public AbstractTrailerRenderer(EntityType<T> type, Supplier<VehicleProperties> defaultProperties)
    {
        super(type, defaultProperties);
    }
}
