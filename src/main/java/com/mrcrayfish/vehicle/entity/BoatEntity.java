package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.common.SurfaceHelper;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class BoatEntity extends PoweredVehicleEntity
{
    protected State state;
    protected State previousState;
    private double waterLevel;
    protected Vec3 velocity = Vec3.ZERO;


    public BoatEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.setSteeringAngle(65);
    }

    @Override
    public boolean canChangeWheels()
    {
        return false;
    }

    @Override
    protected void onVehicleTick()
    {
        this.previousState = this.state;
        this.state = this.getState();

//        if(this.state == State.IN_AIR)
//        {
//            this.deltaYaw *= 2;
//        }
    }

    private void applyBuoyancy()
    {
        /*
         * waterLevel is the highest water surface found by checkInWater().
         */
        double targetY =
                this.waterLevel
                        - 0.35D
                        + (0.25D * Mth.clamp(
                        (float) (
                                this.velocity.horizontalDistance()
                                        / Math.max(0.01F, this.getMaxSpeed())
                        ),
                        0.0F,
                        1.0F
                ));

        double verticalError =
                targetY - this.getY();

        /*
         * Buoyancy behaves like a spring:
         *
         * too low  -> push upward
         * too high -> weak downward correction
         */
        double buoyancyForce =
                Mth.clamp(
                        verticalError * 0.12D,
                        -0.08D,
                        0.08D
                );

        /*
         * Dampen vertical motion so the boat doesn't bounce forever.
         */
        double verticalVelocity =
                this.getDeltaMovement().y() * 0.75D;

        this.setDeltaMovement(
                this.getDeltaMovement().x(),
                verticalVelocity + buoyancyForce,
                this.getDeltaMovement().z()
        );
    }

    private void applyWaterDrag()
    {
        /*
         * Used when the boat has no fuel.
         * It should stop accelerating but continue floating and
         * gradually coast to a stop.
         */
        this.velocity =
                new Vec3(
                        this.velocity.x * 0.92D,
                        this.velocity.y,
                        this.velocity.z * 0.92D
                );

        this.motion =
                new Vec3(
                        this.velocity.x,
                        0.0D,
                        this.velocity.z
                );
    }

    @Override
    public void updateVehicleMotion()
    {
        boolean isCreative =
                this.getControllingPassenger() instanceof Player player
                        && player.isCreative();

        this.state = this.getState();

        /*
         * ------------------------------------------------------------
         * 1. BASIC VALIDITY / FUEL
         * ------------------------------------------------------------
         */

        if(!this.canDrive())
        {
            this.setThrottle(0F);
            this.velocity = Vec3.ZERO;
            this.motion = Vec3.ZERO;
            return;
        }

        if(this.requiresFuel() && !isCreative && !this.isFueled())
        {
            this.setThrottle(0F);

            /*
             * Still allow buoyancy / water physics while out of fuel.
             * The boat should float instead of freezing completely.
             */
            this.applyBuoyancy();

            this.applyWaterDrag();

            this.motion = this.velocity;

            if(this.getControllingPassenger() instanceof Player player
                    && this.tickCount % 40 == 0)
            {
                CommonUtils.sendInfoMessage(
                        player,
                        "vehicle.status.out_of_fuel"
                );
            }

            return;
        }

        /*
         * ------------------------------------------------------------
         * 2. AIR / UNDERWATER / WATER
         * ------------------------------------------------------------
         */

        if(this.state == State.IN_AIR)
        {
            /*
             * Preserve some horizontal momentum while airborne.
             */
            this.velocity = new Vec3(
                    this.velocity.x * 0.985D,
                    this.velocity.y,
                    this.velocity.z * 0.985D
            );

            /*
             * Gravity.
             */
            this.setDeltaMovement(
                    this.getDeltaMovement().add(0, -0.08, 0)
            );

            /*
             * If we just left the water, don't suddenly lose horizontal
             * velocity.
             */
            if(this.previousState == State.IN_WATER
                    || this.previousState == State.UNDER_WATER
                    || this.previousState == State.UNDER_FLOWING_WATER)
            {
                this.velocity = new Vec3(
                        this.velocity.x,
                        this.velocity.y,
                        this.velocity.z
                );
            }

            /*
             * motion is the horizontal movement consumed by the parent
             * tick's move(... + motion).
             */
            this.motion = this.velocity;

            /*
             * IMPORTANT:
             * updateSpeed() runs before this method, so make sure the
             * motion we leave here is the value used next tick.
             */
            return;
        }

        /*
         * ------------------------------------------------------------
         * 3. BUOYANCY
         * ------------------------------------------------------------
         */

        this.applyBuoyancy();

        /*
         * Fully underwater:
         *
         * Don't let the engine behave like a normal surface boat.
         * Keep horizontal movement but heavily reduce propulsion.
         */
        boolean underwater =
                this.state == State.UNDER_WATER
                        || this.state == State.UNDER_FLOWING_WATER;

        /*
         * ------------------------------------------------------------
         * 4. WATER PROPULSION
         * ------------------------------------------------------------
         */

        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        Vec3 horizontalForward = new Vec3(forward.x, 0.0, forward.z);

        if(horizontalForward.lengthSqr() > 1.0E-7)
        {
            horizontalForward = horizontalForward.normalize();
        }

        float throttle =
                this.isHandbraking()
                        ? 0.0F
                        : Mth.clamp(this.getThrottle(), -1.0F, 1.0F);

        /*
         * Boats should accelerate much more gently than land vehicles.
         */
        float enginePower = this.getEnginePower();

        /*
         * Forward/reverse propulsion.
         */
        float reverseMultiplier = throttle < 0.0F ? 0.45F : 1.0F;

        double acceleration =
                enginePower
                        * throttle
                        * reverseMultiplier
                        * 0.0125D;

        /*
         * Boost support.
         *
         * This uses the same boost state already maintained by the
         * PoweredVehicleEntity.
         */
        if(this.isBoosting() && throttle > 0.0F)
        {
            acceleration += acceleration * this.getSpeedMultiplier();
        }

        /*
         * Underwater propulsion is intentionally weaker.
         */
        if(underwater)
        {
            acceleration *= 0.35D;
        }

        Vec3 engineForce = horizontalForward.scale(acceleration);

        /*
         * ------------------------------------------------------------
         * 5. WATER FRICTION / DRAG
         * ------------------------------------------------------------
         */

        /*
         * Higher drag at higher velocity gives the boat a smooth
         * acceleration curve and prevents it from accelerating forever.
         */
        double speed = this.velocity.horizontalDistance();

        /*
         * Base water drag.
         */
        double dragCoefficient = underwater ? 0.045D : 0.025D;

        /*
         * Quadratic drag.
         *
         * dragForce ~ velocity * velocity
         */
        Vec3 dragForce = Vec3.ZERO;

        if(speed > 1.0E-5)
        {
            dragForce = this.velocity
                    .scale(-dragCoefficient * speed);
        }

        /*
         * Additional general water resistance.
         */
        Vec3 waterFriction =
                this.velocity.scale(underwater ? -0.03D : -0.012D);

        /*
         * ------------------------------------------------------------
         * 6. STEERING / TURNING
         * ------------------------------------------------------------
         */

        float steering = this.getSteeringAngle();

        if(Math.abs(steering) > 0.001F && speed > 0.02D)
        {
            /*
             * Steering input is already signed:
             *   negative = one direction
             *   positive = the other direction
             */
            float direction = this.velocity.dot(horizontalForward) < 0.0D ? -1.0F : 1.0F;

            float turnSpeed =
                    (steering / 65.0F)
                            * 6.0F
                            * (float)Math.min(
                            1.0D,
                            speed / (this.getMaxSpeed() / 20.0D)
                    );

            this.setYRot(
                    this.getYRot() - turnSpeed * direction
            );

            forward =
                    Vec3.directionFromRotation(this.getRotationVector());

            horizontalForward =
                    new Vec3(forward.x, 0.0D, forward.z);

            if(horizontalForward.lengthSqr() > 1.0E-7D)
            {
                horizontalForward = horizontalForward.normalize();
            }
        }

        /*
         * ------------------------------------------------------------
         * 7. APPLY FORCES
         * ------------------------------------------------------------
         */

        Vec3 accelerationVector =
                engineForce
                        .add(dragForce)
                        .add(waterFriction);

        this.velocity =
                this.velocity.add(accelerationVector);

        /*
         * ------------------------------------------------------------
         * 8. LATERAL WATER RESISTANCE
         *
         * Boats should not slide sideways like a frictionless object.
         * Preserve forward velocity but damp the sideways component.
         * ------------------------------------------------------------
         */

        double forwardVelocity =
                this.velocity.dot(horizontalForward);

        Vec3 forwardVelocityVector =
                horizontalForward.scale(forwardVelocity);

        Vec3 sidewaysVelocity =
                this.velocity.subtract(forwardVelocityVector);

        /*
         * Strong lateral damping produces a much more boat-like feel.
         */
        sidewaysVelocity =
                sidewaysVelocity.scale(underwater ? 0.70D : 0.82D);

        this.velocity =
                forwardVelocityVector.add(sidewaysVelocity);

        /*
         * ------------------------------------------------------------
         * 9. SPEED LIMIT
         * ------------------------------------------------------------
         */

        double maxSpeedPerSecond = Math.max(0.01D, this.getMaxSpeed());
        double maxSpeedPerTick = maxSpeedPerSecond / 20.0D;

        double currentHorizontalSpeed =
                this.velocity.horizontalDistance();

        if(currentHorizontalSpeed > maxSpeedPerTick)
        {
            double ratio = maxSpeedPerTick / currentHorizontalSpeed;

            this.velocity = new Vec3(
                    this.velocity.x * ratio,
                    this.velocity.y,
                    this.velocity.z * ratio
            );
        }

        /*
         * Reverse speed can be reduced independently.
         */

        double forwardDot = this.velocity.dot(horizontalForward);

        /*
         * ------------------------------------------------------------
         * 10. HAND BRAKE
         * ------------------------------------------------------------
         */

        if(this.isHandbraking())
        {
            /*
             * Strong but smooth slowing rather than an instant stop.
             */
            this.velocity =
                    this.velocity.scale(
                            underwater ? 0.80D : 0.88D
                    );
        }

        /*
         * ------------------------------------------------------------
         * 11. FINAL MOTION
         * ------------------------------------------------------------
         *
         * PoweredVehicleEntity moves:
         *
         * move(..., getDeltaMovement().add(this.motion))
         *
         * So the boat's horizontal velocity goes into motion while
         * vertical movement stays in deltaMovement.
         */
        this.motion = new Vec3(
                this.velocity.x,
                0.0D,
                this.velocity.z
        );

        /*
         * Damp any old horizontal delta movement so it doesn't
         * accidentally add another copy of the boat's velocity.
         */
        this.setDeltaMovement(
                this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D)
        );
    }

    private boolean checkInWater()
    {
        AABB boundingBox = this.getBoundingBox();
        int minX = Mth.floor(boundingBox.minX);
        int maxX = Mth.ceil(boundingBox.maxX);
        int minY = Mth.floor(boundingBox.minY);
        int maxY = Mth.ceil(boundingBox.minY + 0.001D);
        int minZ = Mth.floor(boundingBox.minZ);
        int maxZ = Mth.ceil(boundingBox.maxZ);

        boolean inWater = false;
        this.waterLevel = Double.MIN_VALUE;

        BlockPos.MutableBlockPos pooledMutable =
                new BlockPos.MutableBlockPos();

        for(int x = minX; x < maxX; x++)
        {
            for(int y = minY; y < maxY; y++)
            {
                for(int z = minZ; z < maxZ; z++)
                {
                    pooledMutable.set(x, y, z);

                    FluidState fluidState =
                            this.level().getFluidState(pooledMutable);

                    if(fluidState.is(FluidTags.WATER))
                    {
                        float waterLevel =
                                (float) y
                                        + fluidState.getHeight(
                                        this.level(),
                                        pooledMutable);

                        this.waterLevel =
                                Math.max(
                                        (double) waterLevel,
                                        this.waterLevel
                                );

                        inWater |=
                                boundingBox.minY < (double) waterLevel;
                    }
                }
            }
        }

        return inWater;
    }

    @Nullable
    private State getUnderwaterState()
    {
        AABB axisalignedbb = this.getBoundingBox();

        double height = axisalignedbb.maxY + 0.001D;

        int minX = Mth.floor(axisalignedbb.minX);
        int maxX = Mth.ceil(axisalignedbb.maxX);
        int minY = Mth.floor(axisalignedbb.maxY);
        int maxY = Mth.ceil(height);
        int minZ = Mth.floor(axisalignedbb.minZ);
        int maxZ = Mth.ceil(axisalignedbb.maxZ);

        boolean underWater = false;

        BlockPos.MutableBlockPos pooledMutable =
                new BlockPos.MutableBlockPos();

        for(int x = minX; x < maxX; x++)
        {
            for(int y = minY; y < maxY; y++)
            {
                for(int z = minZ; z < maxZ; z++)
                {
                    pooledMutable.set(x, y, z);

                    FluidState fluidState =
                            this.level().getFluidState(pooledMutable);

                    if(fluidState.is(FluidTags.WATER)
                            && height < (double) (
                            (float) pooledMutable.getY()
                                    + fluidState.getHeight(
                                    this.level(),
                                    pooledMutable)))
                    {
                        if(!fluidState.isSource())
                        {
                            return State.UNDER_FLOWING_WATER;
                        }

                        underWater = true;
                    }
                }
            }
        }

        return underWater
                ? State.UNDER_WATER
                : null;
    }

    protected State getState()
    {
        State state = this.getUnderwaterState();

        if(state != null)
        {
            return state;
        }
        else if(this.checkInWater())
        {
            return State.IN_WATER;
        }
//        else if(this.onGround())
//        {
//            return State.ON_LAND;
//        }
        else
        {
            return State.IN_AIR;
        }
    }

//    @Override
//    protected void updateGroundState()
//    {
//        this.wheelsOnGround =
//                this.getState() == State.IN_WATER
//                        || this.getState() == State.UNDER_WATER;
//    }

    protected enum State
    {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;
    }
}