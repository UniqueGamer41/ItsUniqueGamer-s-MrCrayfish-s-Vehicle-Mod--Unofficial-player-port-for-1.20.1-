package com.mrcrayfish.vehicle.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles rendering the vehicle stats overlay (speed, fuel, etc.)
 * in the top-left corner of the screen.
 *
 * Updated for 1.20.1
 */
public class OverlayHandler
{
    private final List<Component> stats = new ArrayList<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
            return;

        this.stats.clear();

        if (!Config.CLIENT.enabledSpeedometer.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive() || mc.options.hideGui)
            return;

        Player player = mc.player;
        if (player == null)
            return;

        Entity entity = player.getVehicle();
        if (!(entity instanceof PoweredVehicleEntity vehicle))
            return;

        DecimalFormat format = new DecimalFormat("0.0"); // match your screenshot (10.4)
        this.addStat("BPS", format.format(vehicle.getSpeed()));

        if (vehicle.requiresEnergy())
        {
            String fuel = format.format(vehicle.getCurrentEnergy()) + "/" + format.format(vehicle.getEnergyCapacity());
            this.addStat("Fuel", fuel);
        }

        if (!FMLLoader.isProduction())
        {
            if (vehicle instanceof LandVehicleEntity landVehicle)
            {
                String traction = format.format(landVehicle.getTraction());
                this.addStat("Traction", traction);

                Vec3 forward = Vec3.directionFromRotation(landVehicle.getRotationVector());
                float side = (float) landVehicle.getVelocity().normalize().cross(forward.normalize()).length();
                String sideString = format.format(side);
                this.addStat("Side", sideString);
            }
        }
    }

    private void addStat(String label, String value)
    {
        this.stats.add(
                Component.literal(label + ": ")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
                        .append(Component.literal(value).withStyle(ChatFormatting.WHITE))
        );
    }

    /**
     * Renders the overlay text at the correct time in the GUI render cycle.
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // Get the pose stack from the event
        //PoseStack poseStack = event.getPoseStack();

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || stats.isEmpty()) {
            return;
        }

        // Use GuiGraphics to draw text
        // event provides a `getGuiGraphics()` (or you can wrap the poseStack in one)
        var guiGraphics = event.getGuiGraphics();  // if available

        for (int i = 0; i < stats.size(); i++) {
            guiGraphics.drawString(
                    mc.font,
                    stats.get(i),
                    10,
                    10 + 12 * i,
                    0xFFFFFF,
                    false
            );
        }
    }

}






































//package com.mrcrayfish.vehicle.client.handler;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mrcrayfish.vehicle.Config;
//import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
//import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.loading.FMLLoader;
//
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Author: MrCrayfish
// */
//public class OverlayHandler
//{
//    private List<Component> stats = new ArrayList<>();
//
//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event)
//    {
//        if(event.phase != TickEvent.Phase.END)
//            return;
//
//        this.stats.clear();
//
//        if(!Config.CLIENT.enabledSpeedometer.get())
//            return;
//
//        Minecraft mc = Minecraft.getInstance();
//        if(!mc.isWindowActive() || mc.options.hideGui)
//            return;
//
//        Player player = mc.player;
//        if(player == null)
//            return;
//
//        Entity entity = player.getVehicle();
//        if(!(entity instanceof PoweredVehicleEntity vehicle))
//            return;
//
//        DecimalFormat format = new DecimalFormat("0.00");
//        this.addStat("BPS", format.format(vehicle.getSpeed()));
//
//        if(vehicle.requiresEnergy())
//        {
//            String fuel = format.format(vehicle.getCurrentEnergy()) + "/" + format.format(vehicle.getEnergyCapacity());
//            this.addStat("Fuel", fuel);
//        }
//
//        if(!FMLLoader.isProduction())
//        {
//            if(vehicle instanceof LandVehicleEntity landVehicle)
//            {
//                String traction = format.format(landVehicle.getTraction());
//                this.addStat("Traction", traction);
//
//                Vec3 forward = Vec3.directionFromRotation(landVehicle.getRotationVector());
//                float side = (float) landVehicle.getVelocity().normalize().cross(forward.normalize()).length();
//                String sideString = format.format(side);
//                this.addStat("Side", sideString);
//            }
//        }
//    }
//
//    private void addStat(String label, String value)
//    {
//        this.stats.add(
//                Component.literal(label + ": ")
//                        .withStyle(ChatFormatting.BOLD)
//                        .withStyle(ChatFormatting.GOLD)
//                        .append(Component.literal(value)
//                                .withStyle(ChatFormatting.WHITE)));
//    }
//
////    @SubscribeEvent
////    public void onRenderTick(TickEvent.RenderTickEvent event)
////    {
////        if(event.phase != TickEvent.Phase.END)
////            return;
////
////        PoseStack stack = new PoseStack();
////        Minecraft mc = Minecraft.getInstance();
////        for(int i = 0; i < this.stats.size(); i++)
////        {
////
////
////            //mc.font.draw(stack, this.stats.get(i), 10, 10 + 15 * i, 0xFFFFFF);
////
////            // mc.font.draw(stack, this.stats.get(i), 10, 10 + 15 * i, 0xFFFFFF);
////        }
////    }
//
////    @SubscribeEvent
////    public void onRenderTick(TickEvent.RenderTickEvent event)
////    {
////        if(event.phase != TickEvent.Phase.END)
////            return;
////
////        PoseStack stack = new PoseStack();
////        Minecraft mc = Minecraft.getInstance();
////
////        for(int i = 0; i < this.stats.size(); i++)
////        {
////            mc.font.drawInBatch(
////                    this.stats.get(i),
////                    10,
////                    10 + 15 * i,
////                    0xFFFFFF,
////                    true,
////                    stack.last().pose(),
////                    mc.renderBuffers().bufferSource(),
////                    Font.DisplayMode.NORMAL,
////                    0,
////                    15728880
////            );
////        }
////
////        mc.renderBuffers().bufferSource().endBatch();
////    }
//
//    @SubscribeEvent
//    public void onRenderTick(TickEvent.RenderTickEvent event)
//    {
//        // The vanilla rendering method changed in 1.17+
//        if(event.phase != TickEvent.Phase.END)
//            return;
//
//        Minecraft mc = Minecraft.getInstance();
//
//        // Check if there are stats to render, and if the GUI is active
////        if (this.stats.isEmpty() || mc.options.hideGui)
////            return;
//
//        // *** 1. The Crucial Setup: Create a new PoseStack and push the identity matrix. ***
//        // This PoseStack must be independent of the world's transformations.
//        PoseStack stack = new PoseStack();
//
//        // In modern Minecraft, you often need to set the projection matrix for 2D screen rendering.
//        // However, when running *late* in the RenderTickEvent, Minecraft's GUI matrices
//        // are usually already set up. We primarily need to ensure our Poses are correct.
//
//        // Loop and draw the stats
//        for(int i = 0; i < this.stats.size(); i++)
//        {
//            // Use PoseStack's position/matrix directly for screen drawing.
//            // NOTE: drawInBatch is the standard modern way for text rendering.
//            mc.font.drawInBatch(
//                    this.stats.get(i),
//                    10.0F, // X position (float expected in modern Forge)
//                    (float) (10 + 15 * i), // Y position
//                    0xFFFFFF, // Color (unused when component already has style, but good practice)
//                    true, // Shadow
//                    stack.last().pose(), // Get the current transformation matrix
//                    mc.renderBuffers().bufferSource(),
//                    net.minecraft.client.gui.Font.DisplayMode.NORMAL,
//                    0, // Background color (0 for transparent)
//                    15728880 // Packed Lightmap Coords
//            );
//        }
//
//        // *** 2. The Crucial Final Step: Execute the batched commands. ***
//        // The text is only drawn when this line is called.
//        mc.renderBuffers().bufferSource().endBatch();
//    }
//}
