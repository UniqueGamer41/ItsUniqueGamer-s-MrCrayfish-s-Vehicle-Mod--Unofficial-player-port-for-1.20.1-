package com.mrcrayfish.vehicle.client.model.complex;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.PlaneEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ComplexModelDynamicSourceRegistry
{
    private static final Map<ResourceLocation, BiFunction<VehicleEntity, Float, Double>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final BiFunction<VehicleEntity, Float, Double> PLANE_AILERON = register(new ResourceLocation(Reference.MOD_ID, "plane_aileron"), (vehicle, partialTicks) ->
            vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getFlapAngle(partialTicks) : 0.0);

    public static final BiFunction<VehicleEntity, Float, Double> PLANE_PROPELLER = register(new ResourceLocation(Reference.MOD_ID, "plane_propeller"), (vehicle, partialTicks) ->
            vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getElevatorAngle(partialTicks) : 0.0);

    public static final BiFunction<VehicleEntity, Float, Double> PLANE_ELEVATOR = register(new ResourceLocation(Reference.MOD_ID, "plane_elevator"), (vehicle, partialTicks) ->
            vehicle instanceof PlaneEntity ? (double) ((PlaneEntity) vehicle).getElevatorAngle(partialTicks) : 0.0);

    public static final BiFunction<VehicleEntity, Float, Double> HELICOPTER_BLADES = register(new ResourceLocation(Reference.MOD_ID, "helicopter_blades"), (vehicle, partialTicks) ->
            vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getBladeRotation(partialTicks) : 0.0);

    public static final BiFunction<VehicleEntity, Float, Double> HELICOPTER_FORWARDS = register(new ResourceLocation(Reference.MOD_ID, "helicopter_forwards"), (vehicle, partialTicks) ->
            vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getForwards(partialTicks) : 0.0);

    public static final BiFunction<VehicleEntity, Float, Double> HELICOPTER_SIDEWARDS = register(new ResourceLocation(Reference.MOD_ID, "helicopter_sidewards"), (vehicle, partialTicks) ->
            vehicle instanceof HelicopterEntity ? (double) ((HelicopterEntity) vehicle).getSidewards(partialTicks) : 0.0);

    public static BiFunction<VehicleEntity, Float, Double> register(ResourceLocation id, BiFunction<VehicleEntity, Float, Double> function)
    {
        if(!REGISTRY.containsKey(id))
        {
            REGISTRY.put(id, function);
            return function;
        }

        return function;
    }

    public static Optional<BiFunction<VehicleEntity, Float, Double>> getSource(ResourceLocation id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Map<ResourceLocation, BiFunction<VehicleEntity, Float, Double>> getRegistry()
    {
        return ImmutableMap.copyOf(REGISTRY);
    }
}
