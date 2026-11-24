package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.entity.properties.HelicopterProperties;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.datasync.VehicleDataValue;
import com.mrcrayfish.vehicle.network.message.MessageHelicopterInput;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public abstract class HelicopterEntity extends PoweredVehicleEntity
{
    protected static final EntityDataAccessor<Float> LIFT = SynchedEntityData.defineId(HelicopterEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> FORWARD_INPUT = SynchedEntityData.defineId(HelicopterEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> SIDE_INPUT = SynchedEntityData.defineId(HelicopterEntity.class, EntityDataSerializers.FLOAT);

    protected final VehicleDataValue<Float> lift = new VehicleDataValue<>(this, LIFT);
    protected final VehicleDataValue<Float> forwardInput = new VehicleDataValue<>(this, FORWARD_INPUT);
    protected final VehicleDataValue<Float> sideInput = new VehicleDataValue<>(this, SIDE_INPUT);

    protected Vec3 velocity = Vec3.ZERO;
    protected float bladeSpeed;

    @OnlyIn(Dist.CLIENT)
    protected float prevBladeRotation, bladeRotation;

    @OnlyIn(Dist.CLIENT)
    protected float prevJoystickStrafe, joystickStrafe;

    @OnlyIn(Dist.CLIENT)
    protected float prevJoystickForward, joystickForward;

    protected HelicopterEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(LIFT, 0F);
        this.entityData.define(FORWARD_INPUT, 0F);
        this.entityData.define(SIDE_INPUT, 0F);
    }

    /**
     * True if the helicopter has what it needs to run its engine: it's allowed to drive,
     * and either the fuel system is off, the pilot is in creative, or it has fuel.
     */
    protected boolean hasFuelToOperate()
    {
        boolean fuelSystemActive = Config.SERVER.fuelEnabled.get() && this.requiresEnergy();
        if(!fuelSystemActive)
        {
            return true;
        }
        boolean isCreative = this.getControllingPassenger() instanceof Player player && player.isCreative();
        return isCreative || this.isFueled();
    }

    @Override
    public void updateVehicleMotion()
    {
        boolean hasFuel = this.hasFuelToOperate();
        Entity entity = this.getControllingPassenger();

        if(!this.canDrive() || !hasFuel)
        {
            this.setThrottle(0F);
        }

        // Show out of fuel message, but keep running physics so the helicopter can fall
        if(!hasFuel && entity instanceof Player player && this.tickCount % 40 == 0)
        {
            CommonUtils.sendInfoMessage(player, "vehicle.status.out_of_fuel");
        }

        this.motion = Vec3.ZERO;

        // Needs a pilot, permission to drive, and fuel
        boolean operating = this.canDrive() && entity != null && hasFuel;

        if(entity != null && this.isFlying() && operating)
        {
            float deltaYaw = entity.getYHeadRot() % 360.0F - this.getYRot();

            while(deltaYaw < -180.0F)
            {
                deltaYaw += 360.0F;
            }

            while(deltaYaw >= 180.0F)
            {
                deltaYaw -= 360.0F;
            }

            this.yRot += (deltaYaw * this.getRotateStrength());
        }

        this.updateBladeSpeed();

        Vec3 heading = Vec3.ZERO;
        if(this.isFlying())
        {
            // Calculates the movement based on the input from the controlling passenger
            float enginePower = this.getEnginePower();
            Vec3 input = this.getInput();
            if(operating && input.length() > 0)
            {
                Vec3 movementForce = input.scale(enginePower).scale(0.05);
                heading = heading.add(movementForce);
            }

            // Makes the helicopter slowly fall due to it tilting during travel
            Vec3 downForce = new Vec3(0, -1.5F * (this.velocity.multiply(1, 0, 1).scale(20).length() / enginePower), 0).scale(0.05);
            heading = heading.add(downForce);

            // Adds a slight drag to the helicopter as it travels through the air
            Vec3 dragForce = this.velocity.scale(this.velocity.length()).scale(-this.getDrag());
            heading = heading.add(dragForce);
        }
        else
        {
            // Slows the helicopter if it's only the ground
            this.velocity = this.velocity.multiply(0.85, 0, 0.85);
        }

        // Adds gravity and the lift needed to counter it
        float gravity = -1.6F;
        float lift = 1.6F * (this.bladeSpeed / 200F);
        heading = heading.add(0, gravity + lift, 0);

        // Clamps the speed based on the global speed limit
        heading = CommonUtils.clampSpeed(heading.scale(20)).scale(0.05);

        // Lerps the velocity to the new heading
        this.velocity = CommonUtils.lerp(this.velocity, heading, this.getMovementStrength());
        this.motion = this.motion.add(this.velocity);

        this.xRot = this.getPitch();

        // Makes the helicopter fall if it's not being operated by a pilot
        if(!operating)
        {
            this.velocity = this.velocity.add(0, -0.08, 0);
            this.motion = this.motion.add(0, -0.08, 0);
        }
    }

    private float getPitch()
    {
        return -(float) new Vec3(-this.motion.x, 0, this.motion.z).scale(this.getMaxLeanAngle()).yRot((float) Math.toRadians(-(this.yRot + 90))).x;
    }

    protected Vec3 getInput()
    {
        if(this.getControllingPassenger() != null)
        {
            double strafe = Mth.clamp(this.getSideInput(), -1.0F, 1.0F);
            double forward = Mth.clamp(this.getForwardInput(), -1.0F, 1.0F);
            Vec3 input = new Vec3(strafe, 0, forward).yRot((float) Math.toRadians(-this.yRot));
            return input.length() > 1.0 ? input.normalize() : input;
        }
        return Vec3.ZERO;
    }

    protected void updateBladeSpeed()
    {
        if(this.canDrive() && this.getControllingPassenger() != null && this.hasFuelToOperate())
        {
            float enginePower = this.getEnginePower();
            float maxBladeSpeed = this.getMaxBladeSpeed();
            if(this.bladeSpeed < maxBladeSpeed)
            {
                this.bladeSpeed += this.getLift() > 0 ? (enginePower / 4F) : 0.5F;
                if(this.bladeSpeed > maxBladeSpeed)
                {
                    this.bladeSpeed = maxBladeSpeed;
                }
            }
            else
            {
                this.bladeSpeed *= 0.95F;
            }
        }
        else
        {
            this.bladeSpeed *= 0.95F;
        }

        if(this.level().isClientSide())
        {
            this.bladeRotation += this.bladeSpeed;
        }
    }

    protected float getMaxBladeSpeed()
    {
        if(this.getLift() > 0)
        {
            return 200F + this.getEnginePower();
        }
        else if(this.isFlying())
        {
            if(this.getLift() < 0)
            {
                return 150F;
            }
            return 200F;
        }
        return 80F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientUpdate()
    {
        super.onClientUpdate();

        this.prevBladeRotation = this.bladeRotation;

        LivingEntity entity = (LivingEntity) this.getControllingPassenger();
        if(entity != null)
        {
            if(entity instanceof Player)
            {
                if(((Player) entity).isLocalPlayer())
                {
                    float lift = VehicleHelper.getLift();
                    this.setLift(lift);
                    this.setForwardInput(entity.zza);
                    this.setSideInput(entity.xxa);
                    PacketHandler.getPlayChannel().sendToServer(new MessageHelicopterInput(lift, entity.zza, entity.xxa));
                }
            }
        }

        this.prevJoystickStrafe = this.joystickStrafe;
        this.prevJoystickForward = this.joystickForward;

        this.joystickStrafe = Mth.lerp(0.25F, this.joystickStrafe, this.getSideInput());
        this.joystickForward = Mth.lerp(0.25F, this.joystickForward, this.getForwardInput());
    }

    @Override
    protected void updateBodyRotations()
    {
        if(this.isFlying())
        {
            double leanAngle = this.getMaxLeanAngle();
            Vec3 rotation = new Vec3(-this.motion.x, 0, this.motion.z).scale(leanAngle).yRot((float) Math.toRadians(-(this.getYRot() + 90)));
            this.bodyRotationPitch = -(float) rotation.x;
            this.bodyRotationRoll = (float) rotation.z;
        }
        else
        {
            this.bodyRotationPitch *= 0.5;
            this.bodyRotationRoll *= 0.5;
        }
        this.bodyRotationYaw = this.getYRot();
    }

    @Override
    protected void updateEngineSound()
    {
        float normal = Mth.clamp(this.bladeSpeed / 200F, 0.0F, 1.25F) * 0.6F;
        normal += (this.motion.scale(20).length() / this.getEnginePower()) * 0.4F;
        this.enginePitch = this.getMinEnginePitch() + (this.getMaxEnginePitch() - this.getMinEnginePitch()) * Mth.clamp(normal, 0.0F, 1.0F);
        this.engineVolume = this.getControllingPassenger() != null && this.isEnginePowered() ? 0.2F + 0.8F * (this.bladeSpeed / 80F) : 0.001F;
    }

    @Override
    public boolean canApplyYawOffset(Entity passenger)
    {
        return passenger != this.getControllingPassenger();
    }

    @Override
    protected void updateTurning() {}

    @Override
    public double getPassengersRidingOffset()
    {
        return 0;
    }

    /*
     * Overridden to prevent players from taking fall damage when landing a plane
     */

    @Override
    public boolean causeFallDamage(float p_146828_, float distance, @NotNull DamageSource p_146830_) {
        return super.causeFallDamage(p_146828_, distance, p_146830_);
    }

    public float getLift()
    {
        return this.lift.get(this);
    }

    public void setLift(float lift)
    {
        this.lift.set(this, lift);
    }

    public float getForwardInput()
    {
        return this.forwardInput.get(this);
    }

    public void setForwardInput(float input)
    {
        this.forwardInput.set(this, input);
    }

    public float getSideInput()
    {
        return this.sideInput.get(this);
    }

    public void setSideInput(float input)
    {
        this.sideInput.set(this, input);
    }

    public boolean isFlying()
    {
        return !this.onGround();
    }

    public final float getMovementStrength()
    {
        return this.getHelicopterProperties().getMovementStrength();
    }

    public final float getRotateStrength()
    {
        return this.getHelicopterProperties().getRotateStrength();
    }

    public final float getMaxLeanAngle()
    {
        return this.getHelicopterProperties().getMaxLeanAngle();
    }

    public final float getDrag()
    {
        return this.getHelicopterProperties().getDrag();
    }

    protected HelicopterProperties getHelicopterProperties()
    {
        return this.getProperties().getExtended(HelicopterProperties.class);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBladeRotation(float partialTicks)
    {
        return this.prevBladeRotation + (this.bladeRotation - this.prevBladeRotation) * partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSidewards(float partialTicks)
    {
        return this.prevJoystickStrafe + (this.joystickStrafe - this.prevJoystickStrafe) * partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public float getForwards(float partialTicks)
    {
        return this.prevJoystickForward + (this.joystickForward - this.prevJoystickForward) * partialTicks;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        super.writeSpawnData(buffer);
        buffer.writeFloat(this.bladeSpeed);
        buffer.writeDouble(this.velocity.x);
        buffer.writeDouble(this.velocity.y);
        buffer.writeDouble(this.velocity.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        super.readSpawnData(buffer);
        this.bladeSpeed = buffer.readFloat();
        this.velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putFloat("bladeSpeed", this.bladeSpeed);
        CompoundTag velocity = new CompoundTag();
        velocity.putDouble("x", this.velocity.x);
        velocity.putDouble("y", this.velocity.y);
        velocity.putDouble("z", this.velocity.z);
        compound.put("velocity", velocity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        this.bladeSpeed = compound.getFloat("bladeSpeed");
        CompoundTag velocity = compound.getCompound("velocity");
        this.velocity = new Vec3(velocity.getDouble("x"), velocity.getDouble("y"), velocity.getDouble("z"));
    }
}