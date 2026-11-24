package com.mrcrayfish.vehicle.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mrcrayfish.vehicle.common.command.SetCosmeticCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * Author: MrCrayfish
 */
public class ModCommands
{
    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event)
    {
        if(FMLLoader.isProduction())
            return;
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();
        this.registerCommands(dispatcher, event.getServer().isDedicatedServer());
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated)
    {
        if(!dedicated)
        {
            SetCosmeticCommand.register(dispatcher);
        }
    }
}
