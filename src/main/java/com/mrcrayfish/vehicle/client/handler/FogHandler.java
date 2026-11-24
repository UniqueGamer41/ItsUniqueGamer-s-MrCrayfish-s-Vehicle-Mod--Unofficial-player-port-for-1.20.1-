package com.mrcrayfish.vehicle.client.handler;

import com.mojang.blaze3d.shaders.FogShape;
import com.mrcrayfish.vehicle.init.ModFluidTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FogHandler
{
    @SubscribeEvent
    public void onRenderFog(ViewportEvent.RenderFog event)
    {
        Camera camera = event.getCamera();
        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
        Entity entity = camera.getEntity();
        FluidState state = entity.level().getFluidState(camera.getBlockPosition());

        if(state.getFluidType() == ModFluidTypes.FUELIUM.get())
        {

            event.setNearPlaneDistance(-8.0F);
            event.setFarPlaneDistance(96.0F);

            if (entity instanceof LocalPlayer player)
            {
                event.scaleFarPlaneDistance(Math.max(0.25F, player.getWaterVision()));

                Holder<Biome> holder = player.level().getBiome(player.blockPosition());
                if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG))
                {
                    event.scaleFarPlaneDistance(0.85F);
                }
            }

            if (event.getFarPlaneDistance() > renderDistance)
            {
                event.setFarPlaneDistance(renderDistance);
                event.setFogShape(FogShape.CYLINDER);
            }
        }
    }
}
