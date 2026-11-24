package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageHelicopterInput extends PlayMessage<MessageHelicopterInput>
{
	private float lift;
	private float forward;
	private float side;

	// Default constructor for deserialization
	public MessageHelicopterInput() {}

	// Constructor to create a new message
	public MessageHelicopterInput(float lift, float forward, float side)
	{
		this.lift = lift;
		this.forward = forward;
		this.side = side;
	}

	// Encoding the message
	@Override
	public void encode(MessageHelicopterInput message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.lift);
		buffer.writeFloat(message.forward);
		buffer.writeFloat(message.side);
	}

	// Decoding the message
	@Override
	public MessageHelicopterInput decode(FriendlyByteBuf buffer)
	{
		float lift = buffer.readFloat();
		float forward = buffer.readFloat();
		float side = buffer.readFloat();
		return new MessageHelicopterInput(lift, forward, side);
	}

	// Handling the message
	@Override
	public void handle(MessageHelicopterInput message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handleHelicopterInputMessage(context.getPlayer(), message);
		});
		context.setHandled(true);
	}

	// Getters for message data
	public float getLift()
	{
		return this.lift;
	}

	public float getForward()
	{
		return this.forward;
	}

	public float getSide()
	{
		return this.side;
	}
}
