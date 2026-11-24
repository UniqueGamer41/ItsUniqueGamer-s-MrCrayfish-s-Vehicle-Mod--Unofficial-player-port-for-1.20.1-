package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class KeyBinds
{
    public static final IKeyConflictContext RIDING_VEHICLE = new IKeyConflictContext()
    {
        @Override
        public boolean isActive()
        {
            Player player = Minecraft.getInstance().player;
            if(player != null && player.getVehicle() instanceof VehicleEntity)
            {
                return KeyConflictContext.IN_GAME.isActive();
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other == this;
        }
    };

    public static final KeyMapping KEY_HORN = new KeyMapping("key.vehicle.horn", GLFW.GLFW_KEY_H, "key.categories.vehicle");
    public static final KeyMapping KEY_CYCLE_SEATS = new KeyMapping("key.vehicle.cycle_seats", GLFW.GLFW_KEY_C, "key.categories.vehicle");
    public static final KeyMapping KEY_HITCH_TRAILER = new KeyMapping("key.vehicle.hitch_trailer", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.vehicle");
    public static final KeyMapping KEY_DASHBOARD = new KeyMapping("key.vehicle.dashboard", GLFW.GLFW_KEY_G, "key.categories.vehicle");

    static
    {
        KEY_HORN.setKeyConflictContext(RIDING_VEHICLE);
        KEY_CYCLE_SEATS.setKeyConflictContext(RIDING_VEHICLE);
        KEY_HITCH_TRAILER.setKeyConflictContext(RIDING_VEHICLE);
        KEY_DASHBOARD.setKeyConflictContext(RIDING_VEHICLE);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event)
    {
        event.register(KEY_HORN);
        event.register(KEY_HORN);
        event.register(KEY_CYCLE_SEATS);
        event.register(KEY_HITCH_TRAILER);
        event.register(KEY_DASHBOARD);
    }
}
