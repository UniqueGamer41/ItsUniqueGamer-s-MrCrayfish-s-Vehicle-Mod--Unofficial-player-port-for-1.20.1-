package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.BoatEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class JetSkiEntity extends BoatEntity
{
    public JetSkiEntity(EntityType<? extends JetSkiEntity> type, Level worldIn)
    {
        super(type, worldIn);

        this.setMaxSpeed(15F);
        //this.setTurnSensitivity(10);
    }



    @Override
    public void createParticles()
    {
        if(state == State.IN_WATER)
        {
            if(this.getThrottle() > 0)
            {
                for(int i = 0; i < 5; i++)
                {
                    this.level().addParticle(ParticleTypes.SPLASH, this.getX() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), this.getBoundingBox().minY + 0.1D, this.getZ() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), -this.getDeltaMovement().x * 4.0D, 1.5D, -this.getDeltaMovement().z * 4.0D);
                }

                for(int i = 0; i < 5; i++)
                {
                    this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), this.getBoundingBox().minY + 0.1D, this.getZ() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), -this.getDeltaMovement().x * 2.0D, 0.0D, -this.getDeltaMovement().z * 2.0D);
                }
            }
        }
    }


    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}