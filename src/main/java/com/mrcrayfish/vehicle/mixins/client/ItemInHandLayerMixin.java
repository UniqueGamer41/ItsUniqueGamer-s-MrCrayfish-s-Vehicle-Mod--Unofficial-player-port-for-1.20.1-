package com.mrcrayfish.vehicle.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandLayer.class)
class ItemInHandLayerMixin
{
    @Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    void fireRenderHeldItem(ItemInHandRenderer renderer, LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource bufferSource, int seed)
    {
        if (!(entity instanceof Player) || ModDataKeys.GAS_PUMP.getValue((Player) entity).isEmpty())
        {
            renderer.renderItem(entity, stack, displayContext, leftHanded, poseStack, bufferSource, seed);
        }
    }
}
















//package com.mrcrayfish.vehicle.mixins.client;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mrcrayfish.obfuscate.client.Hooks;
//import net.minecraft.client.renderer.ItemInHandRenderer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.block.model.ItemTransforms;
//import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemDisplayContext;
//import net.minecraft.world.item.ItemStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(ItemInHandLayer.class)
//public class ItemInHandLayerMixin
//{
//    @Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
//    public void fireRenderHeldItem(ItemInHandRenderer renderer, LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource source, int light)
//    {
//        Hooks.fireRenderHeldItem(renderer, entity, stack, displayContext, leftHanded, poseStack, source, light);
//    }
//}
