package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = Reference.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class RampModelLoaders
{
    @SubscribeEvent
    public static void onRegisterLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("ramp", ModelRamp.LOADER);
        event.register("steep_ramp", ModelSteepRamp.LOADER);
    }
}