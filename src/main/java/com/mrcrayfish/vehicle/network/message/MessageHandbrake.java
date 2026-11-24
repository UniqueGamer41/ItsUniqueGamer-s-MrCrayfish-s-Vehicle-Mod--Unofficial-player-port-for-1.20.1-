package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageHandbrake extends PlayMessage<MessageHandbrake>
{
	private boolean handbrake;

	// Default constructor for deserialization
	public MessageHandbrake() {}

	// Constructor to create a new message
	public MessageHandbrake(boolean handbrake)
	{
		this.handbrake = handbrake;
	}

	// Encoding the message
	@Override
	public void encode(MessageHandbrake message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.handbrake);
	}

	// Decoding the message
	@Override
	public MessageHandbrake decode(FriendlyByteBuf buffer)
	{
		boolean handbrake = buffer.readBoolean();
		return new MessageHandbrake(handbrake);
	}

	// Handling the message
	@Override
	public void handle(MessageHandbrake message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handleHandbrakeMessage(context.getPlayer(), message);
		});
		context.setHandled(true);
	}

	// Getter for the handbrake state
	public boolean isHandbrake()
	{
		return this.handbrake;
	}
}
