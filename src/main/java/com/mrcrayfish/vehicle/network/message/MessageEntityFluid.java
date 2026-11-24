package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

/**
 * Author: MrCrayfish
 */
public class MessageEntityFluid extends PlayMessage<MessageEntityFluid>
{
    private int entityId;
    private FluidStack stack;

    // Default constructor for deserialization
    public MessageEntityFluid() {}

    // Constructor to create a new message
    public MessageEntityFluid(int entityId, FluidStack stack)
    {
        this.entityId = entityId;
        this.stack = stack;
    }

    // Encoding the message
    @Override
    public void encode(MessageEntityFluid message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.stack.writeToNBT(new CompoundTag()));
    }

    // Decoding the message
    @Override
    public MessageEntityFluid decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        FluidStack stack = FluidStack.loadFluidStackFromNBT(buffer.readNbt());
        return new MessageEntityFluid(entityId, stack);
    }

    // Handling the message
    @Override
    public void handle(MessageEntityFluid message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleEntityFluid(message));
        context.setHandled(true);
    }

    // Getter for the entity ID
    public int getEntityId()
    {
        return this.entityId;
    }

    // Getter for the FluidStack
    public FluidStack getStack()
    {
        return this.stack;
    }
}
