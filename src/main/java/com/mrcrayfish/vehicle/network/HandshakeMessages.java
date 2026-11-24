package com.mrcrayfish.vehicle.network;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.HandshakeMessage;
import com.mrcrayfish.framework.network.message.handshake.*;
//import com.mrcrayfish.*;
//import com.mrcrayfish.framework.api.network.HandshakeMessage;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.properties.VehiclePropertiesDataLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class HandshakeMessages
{
    static class LoginIndexedMessage implements IntSupplier
    {
        private int loginIndex;

        void setLoginIndex(final int loginIndex)
        {
            this.loginIndex = loginIndex;
        }

        int getLoginIndex()
        {
            return loginIndex;
        }

        @Override
        public int getAsInt()
        {
            return getLoginIndex();
        }
    }

    public static class S2CVehicleProperties extends HandshakeMessage<S2CVehicleProperties>
    {
        private ImmutableMap<ResourceLocation, VehicleProperties> propertiesMap;

        public S2CVehicleProperties() {}
        public ImmutableMap<ResourceLocation, VehicleProperties> getPropertiesMap()
        {
            return this.propertiesMap;
        }

        @Override
        public void encode(S2CVehicleProperties s2CVehicleProperties, FriendlyByteBuf buffer)
        {
            VehiclePropertiesDataLoader.get().writeVehicleProperties(buffer);
        }

        @Override
        public S2CVehicleProperties decode(FriendlyByteBuf buffer)
        {
            S2CVehicleProperties message = new S2CVehicleProperties();
            message.propertiesMap = VehiclePropertiesDataLoader.readVehicleProperties(buffer);
            return message;
        }



        @Override
        public void handle(S2CVehicleProperties msg, MessageContext messageContext)
        {
            HandshakeHandler.handleVehicleProperties(msg, (Supplier<NetworkEvent.Context>) messageContext);
        }

    }
}