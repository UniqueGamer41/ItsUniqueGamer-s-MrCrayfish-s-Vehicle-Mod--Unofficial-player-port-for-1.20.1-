package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class ShoppingCartEntity extends LandVehicleEntity
{
    private Player pusher;

    public ShoppingCartEntity(EntityType<? extends ShoppingCartEntity> type, Level worldIn)
    {

        super(type, worldIn);
        this.setRequiresFuel(false);

    }

    @Override
    public void tick()
    {
        if(this.pusher != null)
        {
            this.yRotO = this.getYRot();
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            float x = Mth.sin(-pusher.getYRot() * 0.017453292F) * 1.3F;
            float z = Mth.cos(-pusher.getYRot() * 0.017453292F) * 1.3F;
            this.setPos(pusher.getX() + x, pusher.getY(), pusher.getZ() + z);
            this.xOld = this.getX();
            this.yOld = this.getY();
            this.zOld = this.getZ();
            this.setYRot(pusher.getYRot());
        }
        else
        {
            super.tick();
        }
    }
}
