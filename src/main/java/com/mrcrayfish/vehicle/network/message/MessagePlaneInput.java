package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessagePlaneInput extends PlayMessage<MessagePlaneInput>
{
	private float lift;
	private float forward;
	private float side;

	// Default constructor for deserialization
	public MessagePlaneInput() {}

	// Constructor to create a new message
	public MessagePlaneInput(float lift, float forward, float side)
	{
		this.lift = lift;
		this.forward = forward;
		this.side = side;
	}

	// Encoding the message
	@Override
	public void encode(MessagePlaneInput message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.lift);
		buffer.writeFloat(message.forward);
		buffer.writeFloat(message.side);
	}

	// Decoding the message
	@Override
	public MessagePlaneInput decode(FriendlyByteBuf buffer)
	{
		float lift = buffer.readFloat();
		float forward = buffer.readFloat();
		float side = buffer.readFloat();
		return new MessagePlaneInput(lift, forward, side);
	}

	// Handling the message
	@Override
	public void handle(MessagePlaneInput message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handlePlaneInputMessage(context.getPlayer(), message);
		});
		context.setHandled(true);
	}

	// Getter for lift
	public float getLift()
	{
		return this.lift;
	}

	// Getter for forward
	public float getForward()
	{
		return this.forward;
	}

	// Getter for side
	public float getSide()
	{
		return this.side;
	}
}
