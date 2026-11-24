package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.block.JackBlock;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class JackBlockEntity extends BlockEntitySynced
{
    public static final int MAX_LIFT_PROGRESS = 20;

    private EntityJack jack = null;

    private boolean activated = false;
    public int prevLiftProgress;
    public int liftProgress;

    public JackBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.JACK.get(), pos, state);
    }

    public static void onServerTick(Level level, BlockPos pos, BlockState state, JackBlockEntity entity)
    {
        entity.tick();
    }

    public void setVehicle(VehicleEntity vehicle)
    {
        this.jack = new EntityJack(ModEntities.JACK.get(), this.level, this.worldPosition, 11 * 0.0625, vehicle.getYRot());
        vehicle.startRiding(this.jack, true);
        this.jack.rideTick();
        this.level.addFreshEntity(this.jack);
    }

    @Nullable
    public EntityJack getJack()
    {
        return this.jack;
    }


    protected void tick()
    {
        if(!this.activated && this.liftProgress == 0 && this.prevLiftProgress == 1)
        {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(JackBlock.ENABLED, false), (1 << 0) | (1 << 1));
        }

        this.prevLiftProgress = this.liftProgress;

        if(this.jack == null)
        {
            List<EntityJack> jacks = this.level.getEntitiesOfClass(EntityJack.class, new AABB(this.worldPosition));
            if(jacks.size() > 0)
            {
                this.jack = jacks.get(0);
            }
        }

        if(this.jack != null && (this.jack.getPassengers().isEmpty() || !this.jack.isAlive()))
        {
            this.jack = null;
        }

        if(this.jack != null)
        {
            if(this.jack.getPassengers().size() > 0)
            {
                if(!this.activated)
                {
                    this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_UP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.activated = true;
                    this.level.setBlock(this.worldPosition, this.getBlockState().setValue(JackBlock.ENABLED, true), (1 << 0) | (1 << 1));
                }
            }
            else if(this.activated)
            {
                this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                this.activated = false;
            }
        }
        else if(this.activated)
        {
            this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.activated = false;
        }

        if(this.activated)
        {
            if(this.liftProgress < MAX_LIFT_PROGRESS)
            {
                this.liftProgress++;
                this.moveCollidedEntities();
            }
        }
        else if(this.liftProgress > 0)
        {
            this.liftProgress--;
            this.moveCollidedEntities();
        }
    }

    private void moveCollidedEntities()
    {
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if(state.getBlock() instanceof JackBlock)
        {
            AABB boundingBox = state.getShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
            List<Entity> list = this.level.getEntities(this.jack, boundingBox);
            if(!list.isEmpty())
            {
                for(Entity entity : list)
                {
                    if(entity.getPistonPushReaction() != PushReaction.IGNORE)
                    {
                        AABB entityBoundingBox = entity.getBoundingBox();
                        double posY = boundingBox.maxY - entityBoundingBox.minY;
                        entity.move(MoverType.PISTON, new Vec3(0.0, posY, 0.0));
                    }
                }
            }
        }
    }

    public float getProgress()
    {
        return (float) this.liftProgress / (float) MAX_LIFT_PROGRESS;
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }
}
