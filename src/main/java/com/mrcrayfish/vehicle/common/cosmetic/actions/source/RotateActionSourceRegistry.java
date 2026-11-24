package com.mrcrayfish.vehicle.common.cosmetic.actions.source;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.PlaneEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class RotateActionSourceRegistry
{
    private static final Map<ResourceLocation, RotateActionSource> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final RotateActionSource PROPELLER = register(new RotateActionSource(new ResourceLocation(Reference.MOD_ID, "propeller"), (vehicle, partialTicks) ->
            vehicle instanceof PlaneEntity ? ((PlaneEntity) vehicle).getPropellerRotation(partialTicks) : 0F));

    public static RotateActionSource register(RotateActionSource source)
    {
        if(!REGISTRY.containsKey(source.key()))
        {
            REGISTRY.put(source.key(), source);
            return source;
        }

        return source;
    }

    public static Optional<RotateActionSource> getSource(ResourceLocation id)
    {
        return Optional.ofNullable(REGISTRY.get(id));
    }
}
