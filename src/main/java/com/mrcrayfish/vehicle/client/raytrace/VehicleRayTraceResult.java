package com.mrcrayfish.vehicle.client.raytrace;

import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import com.mrcrayfish.vehicle.entity.VehicleEntity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class VehicleRayTraceResult extends EntityHitResult
{
    private final EntityRayTracer tracer;
    private final RayTraceData data;
    private final double distanceToEyes;
    private final boolean rightClick;

    public VehicleRayTraceResult(VehicleEntity entity, Vec3 hitVec, double distanceToEyes, EntityRayTracer tracer, RayTraceData data, boolean rightClick)
    {
        super(entity, hitVec);
        this.distanceToEyes = distanceToEyes;
        this.tracer = tracer;
        this.data = data;
        this.rightClick = rightClick;
    }

    public RayTraceData getData()
    {
        return this.data;
    }

    public double getDistanceToEyes()
    {
        return this.distanceToEyes;
    }

    public boolean isRightClick()
    {
        return this.rightClick;
    }

    @Nullable
    public InteractionHand performContinuousInteraction(Player player)
    {
        RayTraceFunction func = this.data.getRayTraceFunction();
        if(func != null)
        {
            return func.apply(this.tracer, this, player);
        }

        return null;
    }

    public boolean equalsContinuousInteraction(RayTraceFunction function)
    {
        return function.equals(this.data.getRayTraceFunction());
    }
}