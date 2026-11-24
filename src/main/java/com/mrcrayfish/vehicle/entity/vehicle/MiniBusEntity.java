package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Author: MrCrayfish
 */
public class MiniBusEntity extends LandVehicleEntity
{
    public MiniBusEntity(EntityType<? extends MiniBusEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(10.4F);
    }

    public static void registerInteractionBoxes() {
        EntityRayTracer.instance().registerTransforms(
                ModEntities.MINI_BUS.get(),
                () -> VehicleEntity.createCosmeticTransforms(ModEntities.MINI_BUS.get())
        );
    }


}
