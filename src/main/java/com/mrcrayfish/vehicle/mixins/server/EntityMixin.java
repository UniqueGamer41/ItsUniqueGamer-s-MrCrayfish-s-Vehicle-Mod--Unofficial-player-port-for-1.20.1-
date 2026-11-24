package com.mrcrayfish.vehicle.mixins.server;

import com.mrcrayfish.vehicle.client.handler.CameraHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin
{
    @Inject(method = "turn", at = @At("HEAD"))
    private void onTurn(double x, double y, CallbackInfo ci)
    {
        //CameraHandler.onPlayerTurn(x, y);
    }
}