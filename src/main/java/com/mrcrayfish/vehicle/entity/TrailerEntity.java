package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.TrailerProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class TrailerEntity extends VehicleEntity
{
    public static final EntityDataAccessor<Integer> PULLING_ENTITY = SynchedEntityData.defineId(TrailerEntity.class, EntityDataSerializers.INT);

    @Nullable
    private Entity pullingEntity;

    @OnlyIn(Dist.CLIENT)
    public float wheelRotation;
    @OnlyIn(Dist.CLIENT)
    public float prevWheelRotation;

    public TrailerEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(PULLING_ENTITY, -1);
    }



    @Override
    public void onUpdateVehicle()
    {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x(), motion.y() - 0.08, motion.z());

        if(this.level().isClientSide())
        {
            int entityId = this.entityData.get(PULLING_ENTITY);
            if(entityId != -1)
            {
                Entity entity = this.level().getEntity(this.entityData.get(PULLING_ENTITY));
                if(entity instanceof Player || (entity instanceof VehicleEntity && ((VehicleEntity) entity).canTowTrailers()))
                {
                    this.pullingEntity = entity;
                }
                else if(this.pullingEntity != null)
                {
                    this.pullingEntity = null;
                }
            }
            else if(this.pullingEntity != null)
            {
                this.pullingEntity = null;
            }
        }

        if(this.pullingEntity != null && !this.level().isClientSide())
        {
            double threshold = Config.SERVER.trailerDetachThreshold.get() + Math.abs(this.getHitchOffset() / 16.0) * this.getProperties().getBodyTransform().getScale();
            if(this.pullingEntity.distanceTo(this) > threshold)
            {
                this.level().playSound(null, this.pullingEntity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.pullingEntity = null;
                return;
            }
        }
        this.getHitchOffset();

        if(this.pullingEntity != null)
        {
            if(!this.pullingEntity.isAlive() || (this.pullingEntity instanceof VehicleEntity && ((VehicleEntity) this.pullingEntity).getTrailer() != this))
            {
                this.pullingEntity = null;
                return;
            }
            this.updatePullingMotion(this.pullingEntity);
        }
        else if(!this.level().isClientSide())
        {
            motion = this.getDeltaMovement();
            this.move(MoverType.SELF, new Vec3(motion.x() * 0.75, motion.y(), motion.z() * 0.75));
        }

        this.checkInsideBlocks();
    }

    private void updatePullingMotion(Entity entity)
    {
        Vec3 towBar = entity.position();
        if(entity instanceof VehicleEntity vehicle)
        {
            Vec3 towBarVec = vehicle.getProperties().getTowBarOffset();
            towBarVec = new Vec3(towBarVec.x, towBarVec.y, towBarVec.z).scale(0.0625);
            towBarVec = towBarVec.scale(vehicle.getProperties().getBodyTransform().getScale());
            towBarVec = towBarVec.add(0, 0, vehicle.getProperties().getBodyTransform().getZ());
            towBar = towBar.add(towBarVec.yRot((float) Math.toRadians(-vehicle.getYRot())));
        }

        this.setYRot((float) Math.toDegrees(Math.atan2(towBar.z - this.getZ(), towBar.x - this.getX()) - Math.toRadians(90F)));
        double deltaRot = this.yRotO - this.yRot;
        if (deltaRot < -180.0D)
        {
            this.yRotO += 360.0F;
        }
        else if (deltaRot >= 180.0D)
        {
            this.yRotO -= 360.0F;
        }

        double bodyScale = this.getProperties().getBodyTransform().getScale();
        Vec3 vec = new Vec3(0, 0, this.getHitchOffset() * bodyScale * 0.0625).yRot((float) Math.toRadians(-this.getYRot())).add(towBar);
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(vec.x - this.getX(), motion.y(), vec.z - this.getZ());
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public double getPassengersRidingOffset()
    {
        return 0.0;
    }

    public boolean setPullingEntity(Entity pullingEntity)
    {
        if(pullingEntity instanceof Player || (pullingEntity instanceof VehicleEntity && pullingEntity.getVehicle() == null && ((VehicleEntity) pullingEntity).canTowTrailers()))
        {
            this.pullingEntity = pullingEntity;
            this.entityData.set(PULLING_ENTITY, pullingEntity.getId());
            return true;
        }
        else
        {
            this.pullingEntity = null;
            this.entityData.set(PULLING_ENTITY, -1);
            return false;
        }
    }

    @Nullable
    public Entity getPullingEntity()
    {
        return pullingEntity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = yaw;
        this.lerpPitch = pitch;
        this.lerpSteps = 1;
    }

    public final double getHitchOffset()
    {
        return this.getTrailerProperties().getHitchOffset();
    }

    protected TrailerProperties getTrailerProperties()
    {
        return this.getProperties().getExtended(TrailerProperties.class);
    }

    @Override
    protected boolean canRide(@NotNull Entity entityIn)
    {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateWheelRotations()
    {
        this.prevWheelRotation = this.wheelRotation;

        VehicleProperties properties = this.getProperties();
        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        Vec3 motion = new Vec3(this.getX() - this.xo, 0, this.getZ() - this.zo);
        double direction = forward.dot(motion.normalize());
        float speed = (float) motion.length() * 20;
        double vehicleScale = properties.getBodyTransform().getScale();
        double wheelCircumference = 24.0 * vehicleScale * 1.25F;
        double rotationSpeed = (speed * direction * 16F) / wheelCircumference;
        this.wheelRotation -= rotationSpeed * 20F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getWheelRotation(@Nullable Wheel wheel, float partialTicks)
    {
        return this.prevWheelRotation + (this.wheelRotation - this.prevWheelRotation) * partialTicks;
    }

    @Override
    public float getStepHeight()
    {
        return 1F;
    }
}
