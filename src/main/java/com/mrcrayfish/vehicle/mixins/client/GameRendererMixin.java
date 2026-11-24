package com.mrcrayfish.vehicle.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.handler.CameraHandler;
//import com.mrcrayfish.vehicle.client.handler.AerialCameraHandler; // <--- NEW IMPORT
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.vehicle.SportsPlaneEntity; // <--- NEW IMPORT
import com.mrcrayfish.vehicle.entity.vehicle.CompactHelicopterEntity; // <--- NEW IMPORT
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;", ordinal = 0))
    private void onRenderLevel$getYRot(float delta, long time, PoseStack matrices, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();


                // Use the standard ground/water vehicle setup
                //CameraHandler.setupVehicleCamera(matrices);


    }
}