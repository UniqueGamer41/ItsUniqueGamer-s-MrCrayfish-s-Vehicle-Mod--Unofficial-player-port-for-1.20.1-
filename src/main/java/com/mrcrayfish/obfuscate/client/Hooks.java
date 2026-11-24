package com.mrcrayfish.obfuscate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.obfuscate.client.event.RenderItemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class Hooks
{
    public static void fireRenderHeldItem(ItemInHandRenderer renderer, LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource source, int light)
    {
        float deltaTick = Minecraft.getInstance().getDeltaFrameTime();
        if(!MinecraftForge.EVENT_BUS.post(new RenderItemEvent.Held.Pre(entity, stack, displayContext, poseStack, source, leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT, light, OverlayTexture.NO_OVERLAY, deltaTick)))
        {
            renderer.renderItem(entity, stack, displayContext, leftHanded, poseStack, source, light);
            MinecraftForge.EVENT_BUS.post(new RenderItemEvent.Held.Post(entity, stack, displayContext, poseStack, source, leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT, light, OverlayTexture.NO_OVERLAY, deltaTick));
        }
    }
}
