package com.mrcrayfish.vehicle.client.handler;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.CameraHelper;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.util.port.Quaternion;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Manages changing the point of view of the camera when mounting and dismount vehicles
 *
 * Author: MrCrayfish
 */
public class AerialCameraHandler
{
    private static AerialCameraHandler instance;

    @Nullable
    private CameraType originalPointOfView = null;
    private final CameraHelper cameraHelper = new CameraHelper();

    private AerialCameraHandler() {}

    public static AerialCameraHandler instance()
    {
        if(instance == null)
        {
            instance = new AerialCameraHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event)
    {
        if(!Config.CLIENT.autoPerspective.get())
            return;

        if(!event.getLevel().isClientSide())
            return;

        if(!event.getEntityMounting().equals(Minecraft.getInstance().player))
            return;

        Entity entity = event.getEntityBeingMounted();
        if(!(entity instanceof VehicleEntity))
            return;

        if(event.isMounting())
        {
            this.originalPointOfView = Minecraft.getInstance().options.getCameraType();
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        else
        {
            if(Config.CLIENT.forceFirstPersonOnExit.get())
            {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            }
            else if(this.originalPointOfView != null)
            {
                Minecraft.getInstance().options.setCameraType(this.originalPointOfView);
            }
            this.originalPointOfView = null;
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event)
    {
        if(!Config.CLIENT.autoPerspective.get())
            return;

        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        Entity entity = player.getVehicle();
        if(!(entity instanceof VehicleEntity))
            return;

        if(!Minecraft.getInstance().options.keyTogglePerspective.isDown())
            return;

        this.originalPointOfView = null;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Player player = Minecraft.getInstance().player;
        if(event.phase != TickEvent.Phase.END || player == null)
            return;

        if(player.getVehicle() != null)
            return;

        this.originalPointOfView = null;
    }

    @SubscribeEvent
    public void onFovUpdate(ComputeFovModifierEvent event)
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        Entity ridingEntity = player.getVehicle();
        if(ridingEntity instanceof VehicleEntity)
        {
            event.setNewFovModifier(1.0F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMountEntity(EntityMountEvent event)
    {
        if(!Config.CLIENT.immersiveCamera.get())
            return;

        if(!event.isMounting())
            return;

        if(!(event.getEntityBeingMounted() instanceof VehicleEntity))
            return;

        Entity entity = event.getEntityMounting();
        if(!(entity instanceof Player) || !((Player) entity).isLocalPlayer())
            return;

        this.cameraHelper.load((VehicleEntity) event.getEntityBeingMounted());
    }

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event)
    {
        if(!Config.CLIENT.immersiveCamera.get())
            return;

        if(event.phase != TickEvent.Phase.END)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.level == null || minecraft.player == null)
            return;

        if(minecraft.isPaused())
            return;

        LocalPlayer player = minecraft.player;
        if(!(player.getVehicle() instanceof VehicleEntity vehicle))
            return;

        this.cameraHelper.tick(vehicle, minecraft.options.getCameraType());
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event)
    {
        this.setupVanillaCamera(event.getCamera(), (float) event.getPartialTick());
    }

    public void setupVanillaCamera(Camera info, float partialTicks)
    {
        if(!Config.CLIENT.immersiveCamera.get())
            return;

        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.level == null || minecraft.player == null)
            return;

        LocalPlayer player = minecraft.player;
        if(!(player.getVehicle() instanceof VehicleEntity vehicle))
            return;

        CameraType pointOfView = minecraft.options.getCameraType();
        this.cameraHelper.setupVanillaCamera(info, pointOfView, vehicle, player, partialTicks);
    }

    public static void setupVehicleCamera(PoseStack matrixStack)
    {
        if(!Config.CLIENT.immersiveCamera.get())
            return;

        Camera info = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity entity = info.getEntity();
        if(!(entity instanceof Player) || !(entity.getVehicle() instanceof VehicleEntity))
            return;

        // Undo the rotations created by vanilla
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-(info.getYRot() + 180F)));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-info.getXRot()));

        // Applies quaternion to rotate camera rather than euler angles
        Quaternionf rotation = info.rotation();
        Quaternion quaternion = new Quaternion(rotation);
        quaternion.mul(Vector3f.YP.rotationDegrees(180F));
        quaternion.conjugate();
        matrixStack.mulPose(quaternion);
    }

    public static void setupShaderCamera(Camera info, float partialTicks)
    {
        CameraHandler.instance().setupVanillaCamera(info, partialTicks);
    }

    public static void onPlayerTurn(double x, double y)
    {
        AerialCameraHandler.instance().cameraHelper.turnPlayerView(x, y);
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event)
    {
        if(!Config.CLIENT.debugCamera.get())
            return;
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.level == null)
            return;
        long windowId = Minecraft.getInstance().getWindow().getWindow();
        for(Map.Entry<Integer, BiConsumer<Float, CameraHelper>> entry : DEBUG_CAMERA_KEY_MAP.entrySet())
        {
            if(GLFW.glfwGetKey(windowId, entry.getKey()) == GLFW.GLFW_PRESS)
            {
                entry.getValue().accept((float) event.getScrollDelta() * 0.1F, this.cameraHelper);
                event.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.Key event)
    {
        if(!Config.CLIENT.debugCamera.get())
            return;
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.level == null)
            return;
        if(event.getKey() == GLFW.GLFW_KEY_KP_7 && event.getAction() == GLFW.GLFW_PRESS)
        {
            this.cameraHelper.debugEnableStrength = !this.cameraHelper.debugEnableStrength;
        }
        else if(event.getKey() == GLFW.GLFW_KEY_KP_8 && event.getAction() == GLFW.GLFW_PRESS)
        {
            this.cameraHelper.debugOffsetX = 0F;
            this.cameraHelper.debugOffsetY = 0F;
            this.cameraHelper.debugOffsetZ = 0F;
            this.cameraHelper.debugOffsetPitch = 0F;
            this.cameraHelper.debugOffsetYaw = 0F;
            this.cameraHelper.debugOffsetRoll = 0F;
        }
    }

    private static final Map<Integer, BiConsumer<Float, CameraHelper>> DEBUG_CAMERA_KEY_MAP = Util.make(() -> {
        Map<Integer, BiConsumer<Float, CameraHelper>> map = new HashMap<>();
        map.put(GLFW.GLFW_KEY_KP_1, (value, handler) -> handler.debugOffsetX += value);
        map.put(GLFW.GLFW_KEY_KP_2, (value, handler) -> handler.debugOffsetY += value);
        map.put(GLFW.GLFW_KEY_KP_3, (value, handler) -> handler.debugOffsetZ += value);
        map.put(GLFW.GLFW_KEY_KP_4, (value, handler) -> handler.debugOffsetPitch += value * 10F);
        map.put(GLFW.GLFW_KEY_KP_5, (value, handler) -> handler.debugOffsetYaw += value * 10F);
        map.put(GLFW.GLFW_KEY_KP_6, (value, handler) -> handler.debugOffsetRoll += value * 10F);
        return ImmutableMap.copyOf(map);
    });
}