package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageHorn extends PlayMessage<MessageHorn>
{
	private boolean horn;

	// Default constructor for deserialization
	public MessageHorn() {}

	// Constructor to create a new message
	public MessageHorn(boolean horn)
	{
		this.horn = horn;
	}

	// Encoding the message
	@Override
	public void encode(MessageHorn message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.horn);
	}

	// Decoding the message
	@Override
	public MessageHorn decode(FriendlyByteBuf buffer)
	{
		boolean horn = buffer.readBoolean();
		return new MessageHorn(horn);
	}

	// Handling the message
	@Override
	public void handle(MessageHorn message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handleHornMessage(context.getPlayer(), message);
		});
		context.setHandled(true);
	}

	// Getter for the horn state
	public boolean isHorn()
	{
		return this.horn;
	}
}
