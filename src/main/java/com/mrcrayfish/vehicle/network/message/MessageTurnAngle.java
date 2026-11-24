package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class MessageTurnAngle extends PlayMessage<MessageTurnAngle>
{
	private float angle;

	// Default constructor for deserialization
	public MessageTurnAngle() {}

	// Constructor to create a new message
	public MessageTurnAngle(float angle)
	{
		this.angle = angle;
	}

	// Getter for the angle
	public float getAngle()
	{
		return this.angle;
	}

	// Encoding the message
	@Override
	public void encode(MessageTurnAngle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.angle);
	}

	// Decoding the message
	@Override
	public MessageTurnAngle decode(FriendlyByteBuf buffer)
	{
		float angle = buffer.readFloat();
		return new MessageTurnAngle(angle);
	}

	// Handling the message
	@Override
	public void handle(MessageTurnAngle message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handleTurnAngleMessage(context.getPlayer(), message);
		});
		context.setHandled(true);
	}
}
