package com.mrcrayfish.vehicle.client.model.complex.value;

import com.mrcrayfish.vehicle.entity.VehicleEntity;

/**
 * Author: MrCrayfish
 */
public interface IValue
{
    double getValue(VehicleEntity entity, float partialTicks);
}
