package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.SurfaceHelper;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.PlaneProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.datasync.VehicleDataValue;
import com.mrcrayfish.vehicle.network.message.MessagePlaneInput;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mrcrayfish.vehicle.util.port.Quaternion;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public abstract class PlaneEntity extends PoweredVehicleEntity
{
    protected static final EntityDataAccessor<Float> LIFT = SynchedEntityData.defineId(PlaneEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> FORWARD_INPUT = SynchedEntityData.defineId(PlaneEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> SIDE_INPUT = SynchedEntityData.defineId(PlaneEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> PLANE_ROLL = SynchedEntityData.defineId(PlaneEntity.class, EntityDataSerializers.FLOAT);

    protected final VehicleDataValue<Float> lift = new VehicleDataValue<>(this, LIFT);
    protected final VehicleDataValue<Float> forwardInput = new VehicleDataValue<>(this, FORWARD_INPUT);
    protected final VehicleDataValue<Float> sideInput = new VehicleDataValue<>(this, SIDE_INPUT);
    protected final VehicleDataValue<Float> planeRoll = new VehicleDataValue<>(this, PLANE_ROLL);

    protected Vec3 velocity = Vec3.ZERO;
    protected float propellerSpeed;
    protected float flapAngle;
    protected float elevatorAngle;

    @OnlyIn(Dist.CLIENT)
    protected float propellerRotation;
    @OnlyIn(Dist.CLIENT)
    protected float prevPropellerRotation;
    @OnlyIn(Dist.CLIENT)
    protected float wheelRotationSpeed;
    @OnlyIn(Dist.CLIENT)
    protected float wheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected float prevWheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected float prevFlapAngle;
    @OnlyIn(Dist.CLIENT)
    protected float prevElevatorAngle;

    protected PlaneEntity(EntityType<?> entityType, Level worldIn)
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
        this.entityData.define(PLANE_ROLL, 0F);
    }

    @Override
    public void updateVehicleMotion()
    {
        this.motion = Vec3.ZERO;

        this.updatePropellerSpeed();

        // Updates the planes roll based on input from the player
        this.flapAngle += (this.getSideInput() * this.getMaxFlapAngle() - this.flapAngle) * this.getFlapStrength();
        if(this.getControllingPassenger() != null && this.isFlying())
        {
            float oldPlaneRoll = this.planeRoll.get(this);
            float newPlaneRoll = oldPlaneRoll - this.flapAngle * this.getFlapSensitivity();
            newPlaneRoll = Mth.wrapDegrees(newPlaneRoll);
            this.planeRoll.set(this, newPlaneRoll);
        }
        else
        {
            this.planeRoll.set(this, this.planeRoll.get(this) * 0.9F);
        }

        VehicleProperties properties = this.getProperties();
        float enginePower = this.getEnginePower() * this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
        float friction = this.isFlying() ? 0F : SurfaceHelper.getFriction(this);
        float drag = 0.75F;
        float forwardForce = Math.max((this.propellerSpeed / 200F) - 0.4F, 0F);
        float liftForce = Math.min((float) (this.velocity.length() * 20) / this.getMinimumSpeedToTakeOff(), 1.0F);
        if(this.getControllingPassenger() == null) liftForce /= 2;
        float elevatorForce = this.isFlying() ? liftForce : (float) Math.floor(liftForce);
        this.elevatorAngle += ((this.getMaxElevatorAngle() * this.getLift()) - this.elevatorAngle) * this.getElevatorStrength();

        // Adds delta pitch and yaw to the plane based on the flaps and roll of the plane
        Vec3 vec = Vec3.directionFromRotation(this.elevatorAngle * elevatorForce * this.getElevatorSensitivity(), 0);
        Vector3f elevatorDirection = new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
        elevatorDirection.transform(new Quaternion(Vector3f.ZP, this.planeRoll.get(this), true));
        this.xRot += CommonUtils.pitch(elevatorDirection);

        this.yRot -= CommonUtils.yaw(elevatorDirection);

        // Makes the plane turn slightly when roll is turned to the side
        float planeRoll = this.planeRoll.get(this) % 360;
        float absPlaneRoll = Math.abs(planeRoll);
        if(absPlaneRoll >= 0 && absPlaneRoll <= 90)
        {
            float forwardFactor = 1.0F - Mth.degreesDifferenceAbs(this.getXRot(), 0F) / 90F;
            float turnStrength = 1.0F - (Mth.degreesDifferenceAbs(absPlaneRoll, 45F) / 45F);
            turnStrength *= Math.signum(planeRoll);
            float turnAmount = turnStrength * forwardFactor * this.getMaxTurnAngle();

            this.yRot += turnAmount;
        }

        // Makes the plane fall the closer it is to being sideways
        float fallAmount = 1.0F - Mth.degreesDifferenceAbs(absPlaneRoll, 90F) / 90F;
        this.xRot += Math.abs(fallAmount);

        // Updates the accelerations of the plane with drag and friction applied
        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        Vec3 acceleration = forward.scale(forwardForce).scale(enginePower).scale(0.05);
        Vec3 dragForce = this.velocity.scale(this.velocity.length()).scale(-drag);
        acceleration = acceleration.add(dragForce);
        Vec3 frictionForce = this.velocity.scale(-friction).scale(0.05);
        acceleration = acceleration.add(frictionForce);
        this.velocity = this.velocity.add(acceleration);

        // Add gravity but is countered based on the lift force
        this.velocity = this.velocity.add(0, -0.08 * (1.0F - liftForce), 0);

        // Clamps the speed based on the global speed limit
        this.velocity = CommonUtils.clampSpeed(this.velocity.scale(20)).scale(0.05);

        // Different physics when on the ground
        if(this.onGround() && this.getFrontAxleOffset() != null && this.getRearAxleOffset() != null)
        {
            // Gets the new position of the wheels
            Transform bodyPosition = properties.getBodyTransform();
            double frontAxleOffset = (bodyPosition.getZ() + this.getFrontAxleOffset().z) * 0.0625 * bodyPosition.getScale();
            double rearAxleOffset = (bodyPosition.getZ() + this.getRearAxleOffset().z) * 0.0625 * bodyPosition.getScale();
            Vec3 worldFrontWheel = this.position().add(forward.scale(frontAxleOffset));
            Vec3 worldRearWheel = this.position().add(forward.scale(rearAxleOffset));
            worldFrontWheel = worldFrontWheel.add(this.velocity.yRot((float) Math.toRadians(this.getSteeringAngle())));
            worldRearWheel = worldRearWheel.add(this.velocity);

            // Updates the delta movement based on the new wheel positions
            Vec3 heading = worldFrontWheel.subtract(worldRearWheel).normalize();
            Vec3 nextPosition = worldRearWheel.add(heading.scale(-rearAxleOffset));
            Vec3 nextMovement = nextPosition.subtract(this.position());
            this.motion = this.motion.add(nextMovement);

            // Updates the velocity based on the new heading
            if(heading.dot(this.velocity.normalize()) > 0)
            {
                this.velocity = CommonUtils.lerp(this.velocity, heading.scale(this.velocity.multiply(1, 0, 1).length()), 0.5F);
            }

            // Calculates the difference from the old yaw to the new yaw
            float vehicleDeltaYaw = CommonUtils.yaw(forward) - CommonUtils.yaw(heading);
            vehicleDeltaYaw = Mth.wrapDegrees(vehicleDeltaYaw);

            this.yRot -= vehicleDeltaYaw;
        }
        else
        {
            // Finally adds velocity to the motion
            this.motion = this.motion.add(this.velocity);
        }

        // Updates the pitch and yaw based on the velocity
        if(this.isFlying())
        {
            float pitchDelta = Mth.degreesDifference(90F, Math.abs(this.xRotO));
            float yawDelta = (float) Math.floor(Math.abs(CommonUtils.yaw(this.motion) - this.getYRot()));
            boolean flipped = this.motion.multiply(1, 0, 1).length() > 0 && yawDelta > 45F && yawDelta <= 180F;
            this.xRot = -CommonUtils.pitch(this.motion);
            this.setYRot(this.motion.multiply(1, 0, 1).length() > 0 ? CommonUtils.yaw(this.motion) : this.getYRot());
            if(flipped)
            {
                pitchDelta += Mth.degreesDifference(90F, Math.abs(this.getXRot()));
                this.xRotO = this.xRotO + pitchDelta * -Math.signum(this.xRotO);
                this.yRotO = Mth.wrapDegrees(this.yRotO + yawDelta);
                this.planeRoll.set(this, this.planeRoll.get(this) + 180F);
                this.getPassengers().forEach(this::updatePassengerPosition);
                if(this.level().isClientSide())
                {
                    this.bodyRotationPitch = this.xRotO;
                    this.bodyRotationYaw = this.yRotO;
                    this.bodyRotationRoll += 180F;
                    this.getPassengers().forEach(this::onPassengerTurned);
                }
            }
        }
        else
        {
            this.xRot = 0F;
        }
    }

    protected void updatePropellerSpeed()
    {
        if(this.canDrive() && this.getControllingPassenger() != null)
        {
            float enginePower = this.getEnginePower();
            enginePower *= this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
            float maxRotorSpeed = this.getMaxRotorSpeed();
            float angleOfAttack = (Mth.clamp(this.getXRot(), -90F, 90F) + 90F) / 180F;
            enginePower *= angleOfAttack;

            // Makes the plane slow down the closer it points up
            if(this.getXRot() < 0)
            {
                float upFactor = 1.0F - (float) Math.pow(1.0F - angleOfAttack / 0.5F, 5);
                maxRotorSpeed = Mth.clamp(maxRotorSpeed * upFactor, Math.min(maxRotorSpeed, 90F), Math.max(maxRotorSpeed, 90F));
            }
            else
            {
                float downFactor = (float) Math.pow(angleOfAttack, 3);
                maxRotorSpeed += maxRotorSpeed * 0.4F * downFactor;
            }

            if(this.propellerSpeed <= maxRotorSpeed)
            {
                this.propellerSpeed += this.getThrottle() > 0 ? Math.sqrt(enginePower) / 5F : 0.4F;
                if(this.propellerSpeed > maxRotorSpeed)
                {
                    this.propellerSpeed = maxRotorSpeed;
                }
            }
            else
            {
                float brakeForce = this.getThrottle() < 0 ? 0.1F : 0.05F;
                this.propellerSpeed = Mth.lerp(brakeForce, this.propellerSpeed, maxRotorSpeed);
            }
        }
        else
        {
            float maxRotorSpeed = this.isFlying() ? 90F : 0F;
            this.propellerSpeed = Mth.lerp(0.05F, this.propellerSpeed, maxRotorSpeed);
        }

        if(this.level().isClientSide())
        {
            this.propellerRotation += this.propellerSpeed;
        }
    }

    protected float getMaxRotorSpeed()
    {
        if(this.getThrottle() > 0)
        {
            return 200F + this.getEnginePower();
        }
        else if(this.isFlying())
        {
            if(this.getThrottle() < 0)
            {
                return 140F;
            }
            return 180F;
        }
        return 70F;
    }

    @Override
    public void onClientUpdate()
    {
        super.onClientUpdate();

        this.prevWheelRotation = this.wheelRotation;
        this.prevPropellerRotation = this.propellerRotation;
        this.prevFlapAngle = this.flapAngle;
        this.prevElevatorAngle = this.elevatorAngle;

        LivingEntity entity = (LivingEntity) this.getControllingPassenger();
        if(entity instanceof Player && ((Player) entity).isLocalPlayer())
        {
            this.setLift(VehicleHelper.getElevator());
            this.setForwardInput(entity.zza);
            this.setSideInput(entity.xxa);
            PacketHandler.getPlayChannel().sendToServer(new MessagePlaneInput(this.lift.getLocalValue(), entity.zza, entity.xxa));
        }
    }

    @Override
    protected void updateBodyRotations()
    {
        if(this.isFlying())
        {
            this.bodyRotationPitch = this.getXRot();
            this.bodyRotationRoll = this.planeRoll.get(this);
        }
        else
        {
            this.bodyRotationPitch *= 0.75F;
            this.bodyRotationRoll *= 0.75F;
        }
        this.bodyRotationYaw = this.getYRot();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Lift", this.getLift());
        compound.putFloat("PlaneRoll", this.planeRoll.getLocalValue());
        compound.putFloat("PropellerSpeed", this.propellerSpeed);
        compound.putFloat("FlapAngle", this.flapAngle);
        compound.putFloat("ElevatorAngle", this.elevatorAngle);
        CompoundTag velocity = new CompoundTag();
        velocity.putDouble("X", this.velocity.x);
        velocity.putDouble("Y", this.velocity.y);
        velocity.putDouble("Z", this.velocity.z);
        compound.put("Velocity", velocity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        this.setLift(compound.getFloat("Lift"));
        this.planeRoll.set(this, compound.getFloat("PlaneRoll"));
        this.propellerSpeed = compound.getFloat("PropellerSpeed");
        this.flapAngle = compound.getFloat("FlapAngle");
        this.elevatorAngle = compound.getFloat("ElevatorAngle");
        CompoundTag velocity = compound.getCompound("Velocity");
        this.velocity = new Vec3(velocity.getDouble("X"), velocity.getDouble("Y"), velocity.getDouble("Z"));
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        super.writeSpawnData(buffer);
        buffer.writeDouble(this.velocity.x);
        buffer.writeDouble(this.velocity.y);
        buffer.writeDouble(this.velocity.z);
        buffer.writeFloat(this.planeRoll.get(this));
        buffer.writeFloat(this.propellerSpeed);
        buffer.writeFloat(this.flapAngle);
        buffer.writeFloat(this.elevatorAngle);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        super.readSpawnData(buffer);
        this.velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.planeRoll.set(this, buffer.readFloat());
        this.propellerSpeed = buffer.readFloat();
        this.flapAngle = buffer.readFloat();
        this.elevatorAngle = buffer.readFloat();
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

    /**
     * The minimum speed the plane requires to take off on a flat ground. It's still possible for
     * planes to glide at lower speeds.
     */
    protected final float getMinimumSpeedToTakeOff()
    {
        return this.getPlaneProperties().getMinimumSpeedToTakeOff();
    }

    /**
     * The maximum absolute angle the flaps on the plane can pitch up or down.
     */
    public final float getMaxFlapAngle()
    {
        return this.getPlaneProperties().getMaxFlapAngle();
    }

    /**
     * This determines how quickly the flaps approach it's maximum flap angle.
     */
    public final float getFlapStrength()
    {
        return this.getPlaneProperties().getFlapStrength();
    }

    /**
     * This controls how much of the max flap angle is applied each tick to the planes motion
     */
    public final float getFlapSensitivity()
    {
        return this.getPlaneProperties().getFlapSensitivity();
    }

    /**
     * The maximum absolute angle the elevator on the plane can pitch up or down.
     */
    public final float getMaxElevatorAngle()
    {
        return this.getPlaneProperties().getMaxElevatorAngle();
    }

    /**
     * This determines how quickly the elevator approaches it's maximum elevator angle.
     */
    public final float getElevatorStrength()
    {
        return this.getPlaneProperties().getElevatorStrength();
    }

    /**
     * This controls how much of the max elevator angle is applied each tick to the planes motion
     */
    public final float getElevatorSensitivity()
    {
        return this.getPlaneProperties().getElevatorSensitivity();
    }

    /**
     * Determines the maximum delta angle the plane should turn when it's rolled at the optimum angle
     */
    public final float getMaxTurnAngle()
    {
        return this.getPlaneProperties().getMaxTurnAngle();
    }

    protected final PlaneProperties getPlaneProperties()
    {
        return this.getProperties().getExtended(PlaneProperties.class);
    }

    /*
     * Overridden to prevent players from taking fall damage when landing a plane
     */

    @Override
    public boolean causeFallDamage(float p_146828_, float distance, @NotNull DamageSource p_146830_) {
        return super.causeFallDamage(p_146828_, distance, p_146830_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEngineSound()
    {
        this.enginePitch = this.getMinEnginePitch() + (this.getMaxEnginePitch() - this.getMinEnginePitch()) * Mth.clamp(this.propellerSpeed / 200F, 0.0F, 2.0F);
        this.engineVolume = this.getControllingPassenger() != null && this.isEnginePowered() ? 0.2F + 0.8F * (this.propellerSpeed / 80F) : 0.001F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPropellerRotation(float partialTicks)
    {
        return this.prevPropellerRotation + (this.propellerRotation - this.prevPropellerRotation) * partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public float getFlapAngle(float partialTicks)
    {
        return this.prevFlapAngle + (this.flapAngle - this.prevFlapAngle) * partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public float getElevatorAngle(float partialTicks)
    {
        return this.prevElevatorAngle + (this.elevatorAngle - this.prevElevatorAngle) * partialTicks;
    }

    @Override
    protected void updateTurning()
    {
        if(this.level().isClientSide())
        {
            float targetAngle = this.onGround() ? this.getSteeringAngle() : 0F;
            this.renderWheelAngle = this.renderWheelAngle + (targetAngle - this.renderWheelAngle) * 0.3F;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateWheelRotations()
    {
        VehicleProperties properties = this.getProperties();
        double wheelCircumference = 24.0;
        double vehicleScale = properties.getBodyTransform().getScale();
        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        Vec3 horizontalMotion = this.motion.multiply(1, 0, 1);
        double direction = forward.dot(horizontalMotion.normalize());

        if(this.onGround())
        {
            this.wheelRotationSpeed = (float) (horizontalMotion.length() * direction * 20);
        }
        else
        {
            this.wheelRotationSpeed *= 0.9;
        }

        Optional<Wheel> wheel = properties.getWheels().stream().findAny();
        if(wheel.isPresent())
        {
            double frontWheelCircumference = wheelCircumference * vehicleScale * wheel.get().getScaleY();
            double rotation = (this.wheelRotationSpeed * 16) / frontWheelCircumference;
            this.wheelRotation -= rotation * 20F;
        }
    }

    @Override
    public float getWheelRotation(@Nullable Wheel wheel, float partialTicks)
    {
        return Mth.lerp(partialTicks, this.prevWheelRotation, this.wheelRotation);
    }
}
