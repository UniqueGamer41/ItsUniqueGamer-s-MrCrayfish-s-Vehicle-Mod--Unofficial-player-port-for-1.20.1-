package com.mrcrayfish.vehicle.client.handler;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import com.mrcrayfish.vehicle.client.ClientHandler;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageCycleSeats;
import com.mrcrayfish.vehicle.network.message.MessageHitchTrailer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Manages controller input
 *
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerHandler
{
//    public static final IKeyConflictContext VEHICLE_KEY_CONFLICT = new VehicleKeyConflict();
//    public static final ButtonBinding ACCELERATE = new ButtonBinding(Buttons.A, "vehicle.button.accelerate", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding REVERSE = new ButtonBinding(Buttons.B, "vehicle.button.brake", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding HANDBRAKE = new ButtonBinding(Buttons.RIGHT_BUMPER, "vehicle.button.handbrake", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding HORN = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "vehicle.button.horn", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding HITCH_TRAILER = new ButtonBinding(Buttons.X, "vehicle.button.hitch_trailer", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding RESET_CAMERA = new ButtonBinding(Buttons.SELECT, "vehicle.button.reset_camera", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding CYCLE_SEATS = new ButtonBinding(Buttons.DPAD_LEFT, "vehicle.button.cycle_seats", "button.categories.vehicle", VEHICLE_KEY_CONFLICT);
//
//    public static final IKeyConflictContext AIR_VEHICLE_KEY_CONFLICT = new VehicleKeyConflict();
//    public static final ButtonBinding ASCEND = new ButtonBinding(Buttons.A, "vehicle.button.ascend", "button.categories.vehicle", AIR_VEHICLE_KEY_CONFLICT);
//    public static final ButtonBinding DESCEND = new ButtonBinding(Buttons.B, "vehicle.button.descend", "button.categories.vehicle", AIR_VEHICLE_KEY_CONFLICT);
//
//    public static void init()
//    {
//        BindingRegistry.getInstance().register(ACCELERATE);
//        BindingRegistry.getInstance().register(REVERSE);
//        BindingRegistry.getInstance().register(HANDBRAKE);
//        BindingRegistry.getInstance().register(HORN);
//        BindingRegistry.getInstance().register(HITCH_TRAILER);
//        BindingRegistry.getInstance().register(RESET_CAMERA);
//        BindingRegistry.getInstance().register(ASCEND);
//        BindingRegistry.getInstance().register(DESCEND);
//        BindingRegistry.getInstance().register(CYCLE_SEATS);
//    }
//
//    @SubscribeEvent
//    public void onButtonInput(ControllerEvent.ButtonInput event)
//    {
//        if(Minecraft.getInstance().screen != null)
//            return;
//
//        if(event.getState())
//        {
//            Player player = Minecraft.getInstance().player;
//            if(player == null)
//                return;
//
//            if(!(player.getVehicle() instanceof VehicleEntity))
//                return;
//
//            int button = event.getButton();
//            if(button == ACCELERATE.getButton() || button == REVERSE.getButton() || button == HANDBRAKE.getButton() || button == HORN.getButton())
//            {
//                event.setCanceled(true);
//            }
//            else if(button == HITCH_TRAILER.getButton())
//            {
//                VehicleEntity vehicle = (VehicleEntity) player.getVehicle();
//                if(vehicle.canTowTrailers())
//                {
//                    PacketHandler.getPlayChannel().sendToServer(new MessageHitchTrailer(vehicle.getTrailer() == null));
//                }
//                event.setCanceled(true);
//            }
//            else if(button == RESET_CAMERA.getButton())
//            {
//                player.setYRot(player.getVehicle().getYRot());
//                player.setXRot(15F);
//                event.setCanceled(true);
//            }
//            else if(button == CYCLE_SEATS.getButton())
//            {
//                PacketHandler.getPlayChannel().sendToServer(new MessageCycleSeats());
//                event.setCanceled(true);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void onAvailableActions(GatherActionsEvent event)
//    {
//        Minecraft mc = Minecraft.getInstance();
//        if(mc.screen != null)
//            return;
//
//        Player player = mc.player;
//        if(player == null)
//            return;
//
//        Map<ButtonBinding, Action> actionMap = event.getActions();
//        if(player.getVehicle() instanceof VehicleEntity vehicle)
//        {
//            actionMap.remove(ButtonBindings.ATTACK);
//            actionMap.remove(ButtonBindings.INVENTORY);
//
//            actionMap.put(ButtonBindings.SNEAK, new Action(Component.literal("Exit Vehicle"), Action.Side.LEFT));
//
//            if(vehicle.getProperties().getSeats().size() > 1)
//            {
//                actionMap.put(CYCLE_SEATS, new Action(Component.literal("Cycle Seats"), Action.Side.LEFT));
//            }
//
//            if(vehicle.canTowTrailers())
//            {
//                actionMap.put(HITCH_TRAILER, new Action(Component.literal("Hitch Trailer"), Action.Side.LEFT));
//            }
//
//            if(event.getVisibility() == ActionVisibility.ALL)
//            {
//                actionMap.put(RESET_CAMERA, new Action(Component.literal("Reset Camera"), Action.Side.LEFT));
//                actionMap.put(ACCELERATE, new Action(Component.literal("Accelerate"), Action.Side.RIGHT));
//
//                if(vehicle instanceof PoweredVehicleEntity)
//                {
//                    if(((PoweredVehicleEntity) vehicle).getSpeed() > 0.05F)
//                    {
//                        actionMap.put(REVERSE, new Action(Component.literal("Brake"), Action.Side.RIGHT));
//                    }
//                    else
//                    {
//                        actionMap.put(REVERSE, new Action(Component.literal("Reverse"), Action.Side.RIGHT));
//                    }
//
//                    if(((PoweredVehicleEntity) vehicle).hasHorn())
//                    {
//                        actionMap.put(HORN, new Action(Component.literal("Horn"), Action.Side.RIGHT));
//                    }
//                }
//            }
//
//            if(vehicle instanceof LandVehicleEntity)
//            {
//                actionMap.put(HANDBRAKE, new Action(Component.literal("Handbrake"), Action.Side.RIGHT));
//            }
//            else if(vehicle instanceof HelicopterEntity)
//            {
//                actionMap.put(ASCEND, new Action(Component.literal("Ascend"), Action.Side.RIGHT));
//                actionMap.put(DESCEND, new Action(Component.literal("Descend"), Action.Side.RIGHT));
//            }
//        }
//        else if(player.getVehicle() == null)
//        {
//            if(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY)
//            {
//                Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
//                if(entity instanceof VehicleEntity)
//                {
//                    actionMap.put(ButtonBindings.USE_ITEM, new Action(Component.literal("Ride Vehicle"), Action.Side.RIGHT));
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void onRenderPlayerPreview(RenderPlayerPreviewEvent event)
//    {
//        Player player = Minecraft.getInstance().player;
//        if(player.getVehicle() instanceof VehicleEntity)
//        {
//            event.setCanceled(true);
//        }
//    }

    public static boolean isRightClicking()
    {
        boolean isRightClicking = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        isRightClicking |= ClientHandler.isControllableLoaded() && Controllable.getController() != null && Controllable.getController().getLTriggerValue() != 0.0F;
        return isRightClicking;
    }

//    public static class VehicleKeyConflict implements IKeyConflictContext
//    {
//        @Override
//        public boolean isActive()
//        {
//            return !KeyConflictContext.GUI.isActive();
//        }
//
//        @Override
//        public boolean conflicts(IKeyConflictContext other)
//        {
//            return this == other;
//        }
//    }
}
