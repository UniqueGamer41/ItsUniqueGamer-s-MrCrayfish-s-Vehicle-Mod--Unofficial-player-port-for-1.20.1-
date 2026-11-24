package com.mrcrayfish.vehicle.client.raytrace;

import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.handler.ControllerHandler;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.item.JerryCanItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageFuelVehicle;
import com.mrcrayfish.vehicle.block.entity.GasPumpTankBlockEntity;
import com.mrcrayfish.vehicle.block.entity.GasPumpBlockEntity;
import com.mrcrayfish.vehicle.network.message.MessageInteractKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public interface RayTraceFunction
{
    @Nullable
    InteractionHand apply(EntityRayTracer rayTracer, VehicleRayTraceResult result, Player player);

    /**
     * Checks if fuel can be transferred from a jerry can to a powered vehicle, and sends a packet to do so every other tick, if it can
     *
     * @return whether or not fueling can continue
     */
    RayTraceFunction FUNCTION_FUELING = (rayTracer, result, player) ->
    {
        System.out.println("[FUELING] called, isRightClicking=" + ControllerHandler.isRightClicking());
        System.out.println("[FUELING] HIT at: " + result.getLocation() + " entity: " + result.getEntity());
        System.out.println("[FUELING] FUNCTION CALLED for: " + result.getEntity());
        Entity entity = result.getEntity();
        if(!(entity instanceof PoweredVehicleEntity poweredVehicle))
            return null;

        if(!poweredVehicle.requiresEnergy() || poweredVehicle.getCurrentEnergy() >= poweredVehicle.getEnergyCapacity())
            return null;

        gasPump: if(SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).isPresent() && ControllerHandler.isRightClicking())
        {
            BlockPos pos = SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).get();
            BlockEntity tileEntity = player.level().getBlockEntity(pos);
            if(!(tileEntity instanceof GasPumpBlockEntity))
                break gasPump;

            tileEntity = player.level().getBlockEntity(pos.below());
            if(!(tileEntity instanceof GasPumpTankBlockEntity gasPumpTank))
                break gasPump;

            FluidTank tank = gasPumpTank.getFluidTank();
            FluidStack stack = tank.getFluid();
            if(stack.isEmpty() || !Config.SERVER.validFuels.get().contains(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString()))
                break gasPump;

            if(rayTracer.getContinuousInteractionTickCounter() % 2 == 0)
            {
                PacketHandler.getPlayChannel().sendToServer(new MessageFuelVehicle(result.getEntity().getId(), InteractionHand.MAIN_HAND));
            }
            return InteractionHand.MAIN_HAND;
        }

        for(InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            if(stack.isEmpty() || !(stack.getItem() instanceof JerryCanItem) || !ControllerHandler.isRightClicking())
                continue;

            Optional<IFluidHandlerItem> optional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if(!optional.isPresent())
                continue;

            IFluidHandlerItem handler = optional.get();
            FluidStack fluidStack = handler.getFluidInTank(0);
            if(fluidStack.isEmpty() || !Config.SERVER.validFuels.get().contains(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString()))
                continue;

            if(rayTracer.getContinuousInteractionTickCounter() % 2 == 0)
            {
                PacketHandler.getPlayChannel().sendToServer(new MessageFuelVehicle(entity.getId(), hand));
            }
            return hand;
        }
        return null;
    };

    RayTraceFunction FUNCTION_KEY_INTERACT = (rayTracer, result, player) ->
    {
        System.out.println("FUNCTION KEY INTERACT IS CALLED");
        Entity entity = result.getEntity();
        System.out.println("Entity: " + entity);

        PacketHandler.getPlayChannel().sendToServer(
                new MessageInteractKey(entity)
        );

        return InteractionHand.MAIN_HAND;
    };
}
