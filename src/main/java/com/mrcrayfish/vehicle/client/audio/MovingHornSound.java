package com.mrcrayfish.vehicle.client.audio;

import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class MovingHornSound extends AbstractTickableSoundInstance
{
    private final WeakReference<Player> playerRef;
    private final WeakReference<PoweredVehicleEntity> vehicleRef;

    public MovingHornSound(Player player, PoweredVehicleEntity vehicle, RandomSource source)
    {
        super(vehicle.getHornSound(), SoundSource.NEUTRAL, source);
        this.playerRef = new WeakReference<>(player);
        this.vehicleRef = new WeakReference<>(vehicle);
        this.volume = 0.0F;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    @Override
    public void tick()
    {
        // Minecraft will still tick the sound even after stop has been called
        if(this.isStopped())
            return;

        PoweredVehicleEntity vehicle = this.vehicleRef.get();
        Player player = this.playerRef.get();
        if(vehicle == null || player == null || (!vehicle.getHorn() && this.volume <= 0.05F) || !vehicle.isAlive() || vehicle.getPassengers().isEmpty())
        {
            this.stop();
            return;
        }

        if(vehicle.getHorn())
        {
            this.volume = Mth.lerp(0.6F, this.volume, 1.0F);
        }
        else
        {
            this.volume = Mth.lerp(0.75F, this.volume, 0.0F);
        }

        this.attenuation = vehicle.equals(player.getVehicle()) ? Attenuation.NONE : Attenuation.LINEAR;

        if(!vehicle.equals(player.getVehicle()))
        {
            this.x = (vehicle.getX() + (player.getX() - vehicle.getX()) * 0.65);
            this.y = (vehicle.getY() + (player.getY() - vehicle.getY()) * 0.65);
            this.z = (vehicle.getZ() + (player.getZ() - vehicle.getZ()) * 0.65);
        }
        else
        {
            this.x = vehicle.getX();
            this.y = vehicle.getY();
            this.z = vehicle.getZ();
        }
    }
}
