package com.mrcrayfish.vehicle.util;

import com.mrcrayfish.vehicle.client.CosmeticCache;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.EntityVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.List;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class VehicleUtil
{
    @OnlyIn(Dist.CLIENT)
    public static <T extends VehicleEntity> void registerVehicleRenderer(EntityRenderersEvent.RegisterRenderers event, EntityType<T> type, Function<EntityType<T>, AbstractVehicleRenderer<T>> rendererFunction)
    {
        AbstractVehicleRenderer<T> renderer = rendererFunction.apply(type);
        event.registerEntityRenderer(type, ctx -> new EntityVehicleRenderer<>(ctx, renderer));
        VehicleRenderRegistry.registerVehicleRendererFunction(type, rendererFunction, renderer);
        EntityRayTracer.instance().registerTransforms(type, renderer::getRayTraceTransforms);
        EntityRayTracer.instance().registerDynamicRayTraceData(type, VehicleUtil::getCosmeticsRayTraceData);
    }

    private static <T extends VehicleEntity> List<RayTraceData> getCosmeticsRayTraceData(T vehicle)
    {
        return CosmeticCache.instance().getDataForVehicle(vehicle);
    }
}
