//package com.mrcrayfish.vehicle.mixins.client;
//
//import com.mrcrayfish.vehicle.client.handler.CameraHandler;
//import net.minecraft.client.Camera;
//import net.minecraft.client.Minecraft;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Pseudo;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Pseudo
//@Mixin(targets = "net.optifine.shaders.ShadersRender", remap = false)
//public class ShadersRenderMixin
//{
//    @Inject(method = "updateActiveRenderInfo",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"
//            ),
//            require = 0
//    )
//    private static void onUpdateActiveRenderInfo$setup(Camera camera, Minecraft mc, float delta, CallbackInfo ci)
//    {
//        CameraHandler.setupShaderCamera(camera, delta);
//    }
//}
