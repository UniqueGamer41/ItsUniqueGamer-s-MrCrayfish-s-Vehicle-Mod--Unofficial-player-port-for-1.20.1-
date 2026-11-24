package com.mrcrayfish.vehicle.common.cosmetic.actions.source;

import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public record RotateActionSource(ResourceLocation key, BiFunction<VehicleEntity, Float, Float> valueFunction)
{}
