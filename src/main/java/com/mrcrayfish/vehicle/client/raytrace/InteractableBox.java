package com.mrcrayfish.vehicle.client.raytrace;

import com.mrcrayfish.vehicle.client.raytrace.data.InteractableBoxRayTraceData;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.world.phys.AABB;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class InteractableBox<T extends VehicleEntity>
{
    private final Supplier<AABB> boxSupplier;
    private final BiConsumer<T, Boolean> handler;
    private final Predicate<T> active;
    private final InteractableBoxRayTraceData data;

    public InteractableBox(Supplier<AABB> boxSupplier, BiConsumer<T, Boolean> handler, Predicate<T> active)
    {
        this.boxSupplier = boxSupplier;
        this.handler = handler;
        this.active = active;
        this.data = new InteractableBoxRayTraceData(this);
    }

    public Supplier<AABB> getBoxSupplier()
    {
        return this.boxSupplier;
    }

    public void handle(VehicleEntity entity, boolean rightClick)
    {
        try
        {
            this.handler.accept((T) entity, rightClick);
        }
        catch(ClassCastException e)
        {
            // Wrong entity type, ignore
        }
    }

    public boolean isActive(VehicleEntity entity)
    {
        try
        {
            return this.active.test((T) entity);
        }
        catch(ClassCastException e)
        {
            return false;
        }
    }

    public InteractableBoxRayTraceData getData()
    {
        return this.data;
    }
}