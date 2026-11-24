package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class MessageThrottle extends PlayMessage<MessageThrottle>
{
	private float power;

	// Default constructor for deserialization
	public MessageThrottle() {}

	// Constructor to create a new message
	public MessageThrottle(float power)
	{
		this.power = power;
	}

	// Encoding the message
	@Override
	public void encode(MessageThrottle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.power);
	}

	// Decoding the message
	@Override
	public MessageThrottle decode(FriendlyByteBuf buffer)
	{
		float power = buffer.readFloat();
		return new MessageThrottle(power);
	}

	// Handling the message
	@Override
	public void handle(MessageThrottle message, MessageContext context)
	{
		context.execute(() -> {
			ServerPlayHandler.handleThrottleMessage(Objects.requireNonNull(context.getPlayer()), message);
		});
		context.setHandled(true);
	}

	// Getter for throttle power
	public float getPower()
	{
		return this.power;
	}
}
