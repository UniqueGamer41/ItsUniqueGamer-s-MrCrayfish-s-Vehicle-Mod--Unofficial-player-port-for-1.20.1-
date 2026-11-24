package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.framework.api.network.message.HandshakeMessage;
import com.mrcrayfish.framework.network.message.IMessage;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class HandshakeHandler
{
    private static final Marker VEHICLE_HANDSHAKE = MarkerManager.getMarker("VEHICLE_HANDSHAKE");

    static void handleVehicleProperties(HandshakeMessages.S2CVehicleProperties message, Supplier<NetworkEvent.Context> c)
    {
        VehicleMod.LOGGER.debug(VEHICLE_HANDSHAKE, "Received vehicle properties from server");

        AtomicBoolean updated = new AtomicBoolean(false);
        CountDownLatch block = new CountDownLatch(1);
        c.get().enqueueWork(() ->
        {
            updated.set(VehicleProperties.updateNetworkVehicleProperties(message));
            block.countDown();
        });

        try
        {
            block.await();
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        c.get().setPacketHandled(true);

        if(updated.get())
        {
            VehicleMod.LOGGER.info("Successfully synchronized vehicle properties from server");
            PacketHandler.getHandshakeChannel().sendToPlayer(() -> c.get().getSender(), new HandshakeMessage.Acknowledge());



        }
        else
        {
            VehicleMod.LOGGER.error("Failed to synchronize vehicle properties from server");
            c.get().getNetworkManager().disconnect(Component.literal("Connection closed - [MrCrayfish's Vehicle Mod] Failed to synchronize vehicle properties from server"));
        }
    }
}