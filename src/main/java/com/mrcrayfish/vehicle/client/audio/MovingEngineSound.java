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
public class MovingEngineSound extends AbstractTickableSoundInstance
{
    private final WeakReference<Player> playerRef;
    private final WeakReference<PoweredVehicleEntity> vehicleRef;

    public MovingEngineSound(Player player, PoweredVehicleEntity vehicle, RandomSource source)
    {
        super(vehicle.getEngineSound(), SoundSource.NEUTRAL, source);

        this.playerRef = new WeakReference<>(player);
        this.vehicleRef = new WeakReference<>(vehicle);
        this.volume = 0.0F;
        this.pitch = 0.5F;
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
        if(vehicle == null || player == null || ((vehicle.getControllingPassenger() == null || !vehicle.isEnginePowered()) && this.volume <= 0.05F) || !vehicle.isAlive())
        {
            this.stop();
            return;
        }

        this.volume = Mth.lerp(0.2F, this.volume, vehicle.getEngineVolume());
        this.pitch = Mth.lerp(0.2F, this.pitch, vehicle.getEnginePitch());
        this.attenuation = vehicle.equals(player.getVehicle()) ? Attenuation.NONE : Attenuation.LINEAR;

        if(!vehicle.equals(player.getVehicle()))
        {
            this.x = (float) (vehicle.getX() + (player.getX() - vehicle.getX()) * 0.65);
            this.y = (float) (vehicle.getY() + (player.getY() - vehicle.getY()) * 0.65);
            this.z = (float) (vehicle.getZ() + (player.getZ() - vehicle.getZ()) * 0.65);
        }
        else
        {
            this.x = vehicle.getX();
            this.y = vehicle.getY();
            this.z = vehicle.getZ();
        }
    }
}
