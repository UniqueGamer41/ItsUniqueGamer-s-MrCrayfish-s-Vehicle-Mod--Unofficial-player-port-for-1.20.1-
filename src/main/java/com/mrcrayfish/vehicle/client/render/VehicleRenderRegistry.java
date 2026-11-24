package com.mrcrayfish.vehicle.client.render;

import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public final class VehicleRenderRegistry
{
    private static final Map<EntityType<?>, AbstractVehicleRenderer<?>> RENDERER_MAP = new HashMap<>();
    private static final Map<EntityType<?>, Function<?, ?>> RENDERER_FUNCTION_MAP = new HashMap<>();

    public static synchronized void registerVehicleRendererFunction(EntityType<?> type, Function<?, ?> rendererFunction, AbstractVehicleRenderer<?> defaultRenderer)
    {
        RENDERER_FUNCTION_MAP.put(type, rendererFunction);
        RENDERER_MAP.put(type, defaultRenderer);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static AbstractVehicleRenderer<?> getRendererFunction(EntityType<?> type)
    {
        Function<EntityType<?>, AbstractVehicleRenderer<?>> rendererFunction = (Function<EntityType<?>, AbstractVehicleRenderer<?>>) RENDERER_FUNCTION_MAP.get(type);
        return rendererFunction != null ? rendererFunction.apply(type) : null;
    }

    @Nullable
    public static AbstractVehicleRenderer<?> getRenderer(EntityType<?> type)
    {
        return RENDERER_MAP.get(type);
    }
}
