package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
//import com.mrcrayfish.framework.api.network.FrameworkChannelBuilder;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.network.message.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler
{
    private static FrameworkNetwork playChannel;
//    private static final SimpleChannel HANDSHAKE_CHANNEL = FrameworkChannelBuilder
//            .create(Reference.MOD_ID, "handshake", 1)
//            .registerHandshakeMessage(HandshakeMessages.S2CVehicleProperties.class)
//            .build();
    private static final FrameworkNetwork HANDSHAKE_CHANNEL = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Reference.MOD_ID, "handshake"), 1)
        .registerHandshakeMessage(HandshakeMessages.S2CVehicleProperties.class, false)
        .build();

//    private static final SimpleChannel PLAY_CHANNEL = FrameworkChannelBuilder
//            .create(Reference.MOD_ID, "play", 1)
//            .registerPlayMessage(MessageTurnAngle.class)
//            .registerPlayMessage(MessageHandbrake.class)
//            .registerPlayMessage(MessageHorn.class)
//            .registerPlayMessage(MessageThrowVehicle.class)
//            .registerPlayMessage(MessagePickupVehicle.class)
//            .registerPlayMessage(MessageAttachChest.class)
//            .registerPlayMessage(MessageAttachTrailer.class)
//            .registerPlayMessage(MessageFuelVehicle.class)
//            .registerPlayMessage(MessageInteractKey.class)
//            .registerPlayMessage(MessageHelicopterInput.class)
//            .registerPlayMessage(MessageCraftVehicle.class)
//            .registerPlayMessage(MessageHitchTrailer.class)
//            .registerPlayMessage(MessageSyncStorage.class)
//            .registerPlayMessage(MessageOpenStorage.class)
//            .registerPlayMessage(MessageThrottle.class)
//            .registerPlayMessage(MessageEntityFluid.class)
//            .registerPlayMessage(MessageSyncPlayerSeat.class, NetworkDirection.PLAY_TO_CLIENT)
//            .registerPlayMessage(MessageCycleSeats.class, NetworkDirection.PLAY_TO_SERVER)
//            .registerPlayMessage(MessageSetSeat.class)
//            .registerPlayMessage(MessageSyncHeldVehicle.class, NetworkDirection.PLAY_TO_CLIENT)
//            .registerPlayMessage(MessagePlaneInput.class)
//            .registerPlayMessage(MessageSyncCosmetics.class, NetworkDirection.PLAY_TO_CLIENT)
//            .registerPlayMessage(MessageInteractCosmetic.class)
//            .registerPlayMessage(MessageSyncActionData.class, NetworkDirection.PLAY_TO_CLIENT)
//            .build();

    public static final FrameworkNetwork PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Reference.MOD_ID, "play"), 1)
            .registerPlayMessage(MessageTurnAngle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageHandbrake.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageHorn.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageThrowVehicle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessagePickupVehicle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageAttachChest.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageAttachTrailer.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageFuelVehicle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageInteractKey.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageHelicopterInput.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageCraftVehicle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageHitchTrailer.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageSyncStorage.class, MessageDirection.PLAY_CLIENT_BOUND)
            .registerPlayMessage(MessageOpenStorage.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageThrottle.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageEntityFluid.class, MessageDirection.PLAY_CLIENT_BOUND)
            .registerPlayMessage(MessageSyncPlayerSeat.class, MessageDirection.PLAY_CLIENT_BOUND)
            .registerPlayMessage(MessageCycleSeats.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageSetSeat.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageSyncHeldVehicle.class, MessageDirection.PLAY_CLIENT_BOUND)
            .registerPlayMessage(MessagePlaneInput.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageSyncCosmetics.class, MessageDirection.PLAY_CLIENT_BOUND)
            .registerPlayMessage(MessageInteractCosmetic.class, MessageDirection.PLAY_SERVER_BOUND)
            .registerPlayMessage(MessageSyncActionData.class, MessageDirection.PLAY_CLIENT_BOUND)
            .build();

    public static void init()
    {}

    /**
     * Gets the handshake network channel for MrCrayfish's Vehicle Mod
     */
    public static FrameworkNetwork getHandshakeChannel()
    {
        return HANDSHAKE_CHANNEL;
    }

    /**
     * Gets the play network channel for MrCrayfish's Vehicle Mod
     */
    public static FrameworkNetwork getPlayChannel()
    {
        return PLAY_CHANNEL;
    }
}
