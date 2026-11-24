package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class MessageCraftVehicle extends PlayMessage<MessageCraftVehicle>
{
    private String vehicleId;
    private BlockPos pos;

    // Default constructor
    public MessageCraftVehicle() {}

    public MessageCraftVehicle(String vehicleId, BlockPos pos)
    {
        this.vehicleId = vehicleId;
        this.pos = pos;
    }

    @Override
    public void encode(MessageCraftVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.vehicleId, 128);
        buffer.writeBlockPos(message.pos);
    }

    @Override
    public MessageCraftVehicle decode(FriendlyByteBuf buffer)
    {
        String id = buffer.readUtf(128);
        BlockPos pos = buffer.readBlockPos();
        return new MessageCraftVehicle(id, pos);
    }

    @Override
    public void handle(MessageCraftVehicle message, MessageContext context)
    {

        //System.out.println("[DEBUG] Handle triggered with ID: " + message.vehicleId + ", Pos: " + message.pos);

//        context.execute(() -> ServerPlayHandler.handleCraftVehicleMessage(context.getPlayer(), message));
//        context.setHandled(true);

        if(context.getPlayer() != null){
            context.execute(() -> ServerPlayHandler.handleCraftVehicleMessage(context.getPlayer(), message));
        }
        else {
            System.out.println("Player must not be null, setting context as handled anyway...");
        }

        context.setHandled(true);


    }

    public String getVehicleId()
    {
        return this.vehicleId;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }
}
