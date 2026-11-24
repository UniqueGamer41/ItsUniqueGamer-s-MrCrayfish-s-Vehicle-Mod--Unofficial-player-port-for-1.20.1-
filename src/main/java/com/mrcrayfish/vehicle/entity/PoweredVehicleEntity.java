package com.mrcrayfish.vehicle.entity;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.SurfaceHelper;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.JerryCanItem;
import com.mrcrayfish.vehicle.item.WheelItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.datasync.VehicleDataValue;
import com.mrcrayfish.vehicle.network.message.MessageHandbrake;
import com.mrcrayfish.vehicle.network.message.MessageHorn;
import com.mrcrayfish.vehicle.network.message.MessageThrottle;
import com.mrcrayfish.vehicle.network.message.MessageTurnAngle;
import com.mrcrayfish.vehicle.block.entity.GasPumpTankBlockEntity;
import com.mrcrayfish.vehicle.block.entity.GasPumpBlockEntity;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public abstract class PoweredVehicleEntity extends VehicleEntity implements ContainerListener, MenuProvider
{
    protected static final int MAX_WHEELIE_TICKS = 10;

    protected static final EntityDataAccessor<Float> THROTTLE = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> STEP_HEIGHT = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> HANDBRAKE = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> REQUIRES_FUEL = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> HORN = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Float> TARGET_TURN_ANGLE = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> CURRENT_FUEL = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> TURN_SENSITIVITY = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> NEEDS_KEY = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> KEY_STACK = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> ENGINE_STACK = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Float> CURRENT_SPEED = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> PREV_CURRENT_SPEED = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> MAX_SPEED = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> MAX_TURN_ANGLE = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> ACCELERATION_SPEED = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> ACCELERATION_DIRECTION = SynchedEntityData.defineId(PoweredVehicleEntity.class, EntityDataSerializers.INT);
    // Sensitive variables used for physics
    private final VehicleDataValue<Float> throttle = new VehicleDataValue<>(this, THROTTLE);
    private final VehicleDataValue<Boolean> handbrake = new VehicleDataValue<>(this, HANDBRAKE);
    private final VehicleDataValue<Float> steeringAngle = new VehicleDataValue<>(this, STEERING_ANGLE);

    protected UUID owner;
    protected boolean wheelsOnGround = true;
    public float currentSpeed;
    public float vehicleMotionX;
    public float vehicleMotionY;
    public float wheelAngle;
    public float vehicleMotionZ;
    protected float speedMultiplier;
    protected boolean boosting;
    protected float prevCurrentSpeed;
    protected int boostTimer;
    protected float boostStrength;
    public float deltaYaw;
    protected boolean launching;
    protected int launchingTimer;
    protected boolean disableFallDamage;
    protected boolean charging;
    protected float chargingAmount;
    private double[] wheelPositions;
    private boolean fueling;
    public boolean needs_fueling;
    public Vec3 motion = Vec3.ZERO;
    private SimpleContainer vehicleInventory;
    //protected AccelerationDirection prevAcceleration;

    @OnlyIn(Dist.CLIENT)
    protected float prevRenderWheelAngle, renderWheelAngle;

    @OnlyIn(Dist.CLIENT)
    protected float enginePitch;

    @OnlyIn(Dist.CLIENT)
    protected float engineVolume;

    protected PoweredVehicleEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public PoweredVehicleEntity(EntityType<?> entityType, Level worldIn, double posX, double posY, double posZ)
    {
        this(entityType, worldIn);
        this.setPos(posX, posY, posZ);
    }

//    public AccelerationDirection getAcceleration()
//    {
//        return AccelerationDirection.values()[this.entityData.get(ACCELERATION_DIRECTION)];
//    }

//

    public void setTurnSensitivity(int sensitivity)
    {
        this.entityData.set(TURN_SENSITIVITY, sensitivity);
    }




    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(THROTTLE, 0F);
        this.entityData.define(HANDBRAKE, false);
        this.entityData.define(TURN_SENSITIVITY, 6);
        this.entityData.define(REQUIRES_FUEL, true);
        this.entityData.define(STEERING_ANGLE, 0F);
        this.entityData.define(HORN, false);
        this.entityData.define(CURRENT_FUEL, 0F);
        this.entityData.define(MAX_TURN_ANGLE, 35);
        this.entityData.define(TARGET_TURN_ANGLE, 0F);
        this.entityData.define(NEEDS_KEY, false);
        this.entityData.define(KEY_STACK, ItemStack.EMPTY);
        this.entityData.define(ENGINE_STACK, ItemStack.EMPTY);
        this.entityData.define(STEP_HEIGHT, 1F);

        this.entityData.define(CURRENT_SPEED, 1F);
        this.entityData.define(MAX_SPEED, 10F);
        this.entityData.define(ACCELERATION_SPEED, 0.5F);
        //this.entityData.define(ACCELERATION_DIRECTION, AccelerationDirection.NONE.ordinal());

    }


    public void setMaxTurnAngle(int turnAngle)
    {
        this.entityData.set(MAX_TURN_ANGLE, turnAngle);
    }

    public void setMaxSpeed(float maxSpeed)
    {
        this.entityData.set(MAX_SPEED, maxSpeed);
    }

    public void setRequiresFuel(boolean required){this.entityData.set(REQUIRES_FUEL, required);}

    public boolean getRequiresFuel() {
        return this. entityData.get(REQUIRES_FUEL);
    }

    public float getMaxSpeed()
    {
        return this.entityData.get(MAX_SPEED);
    }

    public SoundEvent getEngineSound()
    {
        return ForgeRegistries.SOUND_EVENTS.getValue(this.getPoweredProperties().getEngineSound());
    }

    public final SoundEvent getHornSound()
    {
        return ForgeRegistries.SOUND_EVENTS.getValue(this.getPoweredProperties().getHornSound());
    }

    public void playFuelPortOpenSound()
    {
        if(!this.fueling)
        {
            this.getFuelFillerType().playOpenSound();
            this.fueling = true;
        }
    }

    public void playFuelPortCloseSound()
    {
        if(this.fueling)
        {
            this.getFuelFillerType().playCloseSound();
            this.fueling = false;
        }
    }

    protected boolean isOnGroundCustom() {
        return this.onGround();
    }

    private boolean isFueling = false;
    private int fuelingTicks = 0;

    public boolean isFueling()
    {
        return this.isFueling;
    }



    public void setFueling(boolean fueling)
    {
        this.isFueling = fueling;
        if(fueling)
        {
            this.fuelingTicks = 0;
        }
    }

    public float getNormalSpeed()
    {
        return this.currentSpeed / this.getMaxSpeed();
    }

    public final float getMinEnginePitch()
    {
        return this.getPoweredProperties().getMinEnginePitch();
    }

    public final float getMaxEnginePitch()
    {
        return this.getPoweredProperties().getMaxEnginePitch();
    }

    public void fuelVehicle(Player player, InteractionHand hand)
    {
        // Set fueling state
        this.setFueling(true);

        if(SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).isPresent())
        {
            BlockPos pos = SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).get();
            BlockEntity tileEntity = this.level().getBlockEntity(pos);
            if(!(tileEntity instanceof GasPumpBlockEntity))
            {
                this.setFueling(false);
                return;
            }

            tileEntity = this.level().getBlockEntity(pos.below());
            if(!(tileEntity instanceof GasPumpTankBlockEntity gasPumpTank))
            {
                this.setFueling(false);
                return;
            }

            FluidTank tank = gasPumpTank.getFluidTank();
            FluidStack stack = tank.getFluid();
            if(stack.isEmpty() || !Config.SERVER.validFuels.get().contains(ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString()))
            {
                this.setFueling(false);
                return;
            }

            stack = tank.drain(200, IFluidHandler.FluidAction.EXECUTE);
            if(stack.isEmpty())
            {
                this.setFueling(false);
                return;
            }

            stack.setAmount(this.addEnergy(stack.getAmount()));
            if(stack.getAmount() <= 0)
            {
                this.setFueling(false);
                return;
            }

            gasPumpTank.getFluidTank().fill(stack, IFluidHandler.FluidAction.EXECUTE);
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if(!(stack.getItem() instanceof JerryCanItem jerryCan))
        {
            this.setFueling(false);
            return;
        }

        Optional<IFluidHandlerItem> optional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
        if(optional.isEmpty())
        {
            this.setFueling(false);
            return;
        }

        IFluidHandlerItem handler = optional.get();
        FluidStack fluidStack = handler.getFluidInTank(0);

        if(fluidStack.isEmpty() || !Config.SERVER.validFuels.get().contains(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString()))
        {
            this.setFueling(false);
            return;
        }

        int transferAmount = Math.min(handler.getFluidInTank(0).getAmount(), jerryCan.getFillRate());
        transferAmount = (int) Math.min(Math.floor(this.getEnergyCapacity() - this.getCurrentEnergy()), transferAmount);
        handler.drain(transferAmount, IFluidHandler.FluidAction.EXECUTE);
        this.addEnergy(transferAmount);

        // Done fueling
        this.setFueling(false);
    }

    /**
     * Checks if the player clicked on the fuel port based on hit position
     */
    /**
     * Checks if the player clicked on the fuel port based on hit position
     */
    /**
     * Checks if the player clicked on the fuel port based on hit position
     */

//    private boolean isFuelPortClicked(Vec3 hitVec)
//    {
//        Transform fuelFillerTransform = this.getPoweredProperties().getFuelFillerTransform();
//
//        if(fuelFillerTransform == null)
//        {
//            return false;
//        }
//
//        // Get the fuel filler position from the transform
//        Vec3 fuelFillerPos = new Vec3(
//                fuelFillerTransform.getX()
//,
//                fuelFillerTransform.getY(),
//                fuelFillerTransform.getZ()
//        );
//
//        // Rotate hit vector to match vehicle rotation
//        Vec3 rotated = hitVec.yRot((float) Math.toRadians(this.getYRot()));
//        // Scale to match vehicle coordinate system (1 unit = 1/16 block)
//        Vec3 localHit = new Vec3(rotated.x * 16.0, rotated.y * 16.0, rotated.z * 16.0);
//
//        // Account for body transform
//        VehicleProperties properties = this.getProperties();
//        Transform bodyTransform = properties.getBodyTransform();
//
//        // Adjust fuel filler position by body transform and offsets
//        Vec3 adjustedFuelFillerPos = fuelFillerPos
//                .add(bodyTransform.getX(), bodyTransform.getY(), bodyTransform.getZ())
//                .add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0);
//
//        // Check distance to fuel filler - MUCH TIGHTER tolerance
//        double distance = adjustedFuelFillerPos.distanceTo(localHit);
//
//        // Reduce from 48 to 8 units (~0.5 blocks instead of 3 blocks)
//        return distance < 8.0;
//    }



    public boolean requiresFuel()
    {
        return Config.SERVER.fuelEnabled.get() && this.entityData.get(REQUIRES_FUEL);
    }


    protected void updateSpeed()
    {
        this.prevCurrentSpeed = this.currentSpeed;

        // Use YOUR motion system instead of deltaMovement
        this.currentSpeed = (float) Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);
    }


    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(!level().isClientSide)
        {
            /* If no owner is set, make the owner the person adding the key. It is used because
             * owner will not be set if the vehicle was summoned through a command */
            if(this.owner == null)
            {
                this.owner = player.getUUID();
            }

            if(stack.getItem() == ModItems.KEY.get())
            {
                if(!this.owner.equals(player.getUUID()))
                {
                    CommonUtils.sendInfoMessage(player, "vehicle.status.invalid_owner");
                    return InteractionResult.FAIL;
                }

                if(this.isLockable())
                {
                    CompoundTag tag = stack.getOrCreateTag();
                    if(!tag.hasUUID("vehicleId") || this.getUUID().equals(tag.getUUID("vehicleId")))
                    {
                        tag.putUUID("vehicleId", this.getUUID());
                        if(!this.isKeyNeeded())
                        {
                            this.setKeyNeeded(true);
                            CommonUtils.sendInfoMessage(player, "vehicle.status.key_added");
                        }
                        else
                        {
                            CommonUtils.sendInfoMessage(player, "vehicle.status.key_created");
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                else
                {
                    CommonUtils.sendInfoMessage(player, "vehicle.status.not_lockable");
                    return InteractionResult.FAIL;
                }
            }
            else if(stack.getItem() == ModItems.WRENCH.get() && this.getVehicle() instanceof EntityJack)
            {
                if(player.getUUID().equals(owner))
                {
                    this.openEditInventory(player);
                }
                else
                {
                    CommonUtils.sendInfoMessage(player, "vehicle.status.invalid_owner");
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand);
    }

//    @Override
//    @NotNull
//    public InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 hitVec, @NotNull InteractionHand hand)
//    {
//        if(player.isCrouching())
//        {
//            return InteractionResult.PASS;
//        }
//
//        // Client-side: just return success
//        if(this.level().isClientSide)
//        {
//            return InteractionResult.SUCCESS;
//        }
//
//        // SERVER-SIDE ONLY: Check for fuel port interaction FIRST
//        // Only show fuel port if fuel system is active and player is not in creative
//        boolean fuelSystemActive = Config.SERVER.fuelEnabled.get() && this.requiresEnergy();
//
//
//        if(fuelSystemActive && this.shouldRenderFuelPort() && this.isFuelPortClicked(hitVec))
//        {
//            ItemStack heldItem = player.getItemInHand(hand);
//
//            // Check if player has an active gas pump OR is holding jerry can
//            boolean hasGasPump = SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP).isPresent();
//            boolean hasJerryCan = heldItem.getItem() instanceof JerryCanItem;
//
//            if(hasJerryCan || hasGasPump)
//            {
//                if(this.getCurrentEnergy() >= this.getEnergyCapacity()){
//                    CommonUtils.sendInfoMessage(player, "vehicle.status.maxcap");
//                    return InteractionResult.PASS;
//                }
////                this.playFuelPortOpenSound();
//                this.setFueling(true);
//                this.fuelVehicle(player, hand);
//                return InteractionResult.CONSUME;
//            }
//
//            // Clicked fuel port but no valid fuel source
//            CommonUtils.sendInfoMessage(player, "vehicle.status.need_fuel_source");
//            return InteractionResult.FAIL;
//        }
//
//        // Fall back to cosmetic interactions and mounting
//        return super.interactAt(player, hitVec, hand);
//    }

    @Override
    public void onUpdateVehicle()
    {
        if(this.level().isClientSide())
        {
            this.onClientUpdate();
        }

        Entity controllingPassenger = this.getControllingPassenger();

        /* If there driver, create particles */
        if(controllingPassenger != null)
        {
            this.createParticles();
        }
        else
        {
            this.setThrottle(0F);
            this.steeringAngle.set(this, this.steeringAngle.get(this) * 0.85F);
        }

        /* Handle the current speed of the vehicle based on rider's forward movement */
        this.updateTurning();
        this.onVehicleTick();
        //this.updateGroundState();
        this.updateVehicle();

        this.updateSpeed();

        /* Updates the vehicle motion */
        this.updateVehicleMotion();

        /* Updates the rotation and fixes the old rotation */
        this.setRot(this.getYRot(), this.getXRot());
        double deltaRot = this.yRotO - this.yRot;
        this.yRotO += (deltaRot < -180) ? 360F : (deltaRot >= 180) ? -360F : 0F;

        this.updateWheelPositions();

        // Move vehicle
        this.move(MoverType.SELF, this.getDeltaMovement().add(this.motion));

        /* Reduces the motion and speed multiplier */
        if(this.onGround())
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.75, 0.0, 0.75));
        }
        else
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 1.0, 0.98));
        }

        if(this.boostTimer > 0 && this.getThrottle() > 0)
        {
            this.boostTimer--;
        }
        else
        {
            this.boostTimer = 0;
            this.boosting = false;
            this.speedMultiplier *= 0.85;
        }

        if(this.launchingTimer > 0)
        {
            //Ensures fall damage is disabled while launching
            this.disableFallDamage = true;
            this.launchingTimer--;
        }
        else
        {
            this.launching = false;
        }

        /* Checks for block collisions */
        this.checkInsideBlocks();

        //TODO improve fuel consumption logic
        if(this.requiresEnergy() && controllingPassenger instanceof Player && !((Player) controllingPassenger).isCreative() && this.isEnginePowered())
        {
            float currentFuel = this.getCurrentEnergy();
            currentFuel -= this.getEnergyConsumptionPerTick() * Config.SERVER.energyConsumptionFactor.get();
            if(currentFuel < 0F) currentFuel = 0F;
            this.setCurrentEnergy(currentFuel);
        }

        // In your vehicle renderer


        if(this.level().isClientSide())
        {
            this.updateEngineSound();
        }
    }
    public double getSpeed()
    {
        return Math.sqrt(Math.pow(this.motion.x, 2) + Math.pow(this.motion.z, 2)) * 20;
    }

    public void updateVehicle() {}


    protected void updateGroundState()
    {
        if(this.hasWheelStack())
        {
            VehicleProperties properties = this.getProperties();
            List<Wheel> wheels = properties.getWheels();
            if(this.hasWheelStack() && wheels != null)
            {
                for(int i = 0; i < wheels.size(); i++)
                {
                    double wheelX = this.wheelPositions[i * 3];
                    double wheelY = this.wheelPositions[i * 3 + 1];
                    double wheelZ = this.wheelPositions[i * 3 + 2];
                    int x = Mth.floor(this.getX() + wheelX);
                    int y = Mth.floor(this.getY() + wheelY - 0.2D);
                    int z = Mth.floor(this.getZ() + wheelZ);
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.level().getBlockState(pos);
                    if(!state.getCollisionShape(this.level(), pos).isEmpty())
                    {
                        wheelsOnGround = true;
                        return;
                    }
                }
            }
            wheelsOnGround = false;
        }
    }

    protected void onVehicleTick() {}

    protected abstract void updateVehicleMotion();

    public final FuelFillerType getFuelFillerType()
    {
        return this.getPoweredProperties().getFuelFillerType();
    }

    public float getTargetTurnAngle()
    {
        return this.entityData.get(TARGET_TURN_ANGLE);
    }

    protected void updateTurning() {}

    protected boolean showWheelParticles()
    {
        return this.getThrottle() > 0 || this.charging || this.boosting;
    }

    protected boolean showTyreSmokeParticles()
    {
        return this.charging || this.boosting;
    }

    public void createParticles()
    {
        if(this.showWheelParticles())
        {
            /* Uses the same logic when rendering wheels to determine the position, then spawns
               particles at the contact of the wheel and the ground.
            */
            VehicleProperties properties = this.getProperties();
            if(properties.getWheels() != null)
            {
                double[] wheelPositions = this.getWheelPositions();
                List<Wheel> wheels = properties.getWheels();
                for(int i = 0; i < wheels.size(); i++)
                {
                    Wheel wheel = wheels.get(i);
                    if(!wheel.shouldSpawnParticles())
                        continue;
                    /* Gets the block under the wheel and spawns a particle */
                    double wheelX = wheelPositions[i * 3];
                    double wheelY = wheelPositions[i * 3 + 1];
                    double wheelZ = wheelPositions[i * 3 + 2];
                    int x = Mth.floor(this.getX() + wheelX);
                    int y = Mth.floor(this.getY() + wheelY - 0.2D);
                    int z = Mth.floor(this.getZ() + wheelZ);
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.level().getBlockState(pos);
                    if(!state.isAir() && state.isSolid())
                    {
                        Vec3 dirVec = this.calculateViewVector(this.getXRot(), this.getYRot() + 180F).add(0, this.charging ? 0.5 : 1.0, 0);
                        if(this.charging)
                        {
                            dirVec = dirVec.scale(this.chargingAmount * this.getEnginePower() / 3F);
                        }
                        if(this.level().isClientSide())
                        {
                            double wheelWorldX = this.getX() + wheelX;
                            double wheelWorldY = this.getY() + wheelY;
                            double wheelWorldZ = this.getZ() + wheelZ;
                            VehicleHelper.spawnWheelParticle(pos, state, wheelWorldX, wheelWorldY, wheelWorldZ, dirVec);
                            if(this.showTyreSmokeParticles() && SurfaceHelper.getSurfaceType(state) == SurfaceHelper.SurfaceType.SOLID)
                            {
                                VehicleHelper.spawnSmokeParticle(wheelWorldX, wheelWorldY, wheelWorldZ, dirVec.multiply(0.03 * this.random.nextFloat(), 0.03, 0.03 * this.random.nextFloat()));
                            }
                        }
                    }
                }
            }
        }

        if(this.shouldShowExhaustFumes() && this.canDrive() && this.tickCount % 2 == 0)
        {
            //TODO maybe add more control of this
            Vec3 fumePosition = this.getExhaustFumesPosition().scale(0.0625).yRot(-this.getYRot() * 0.017453292F);

            this.level().addParticle(ParticleTypes.SMOKE, this.getX() + fumePosition.x(), this.getY() + fumePosition.y(), this.getZ() + fumePosition.z(), -this.getDeltaMovement().x, 0.0D, -this.getDeltaMovement().z);
            if(this.charging && this.isMoving())
            {
                this.level().addParticle(ParticleTypes.CRIT, this.getX() + fumePosition.x(), this.getY() + fumePosition.y(), this.getZ() + fumePosition.z(), -this.getDeltaMovement().x, 0.0D, -this.getDeltaMovement().z);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClientUpdate()
    {
        this.prevRenderWheelAngle = this.renderWheelAngle;

        Entity entity = this.getControllingPassenger();
        if(entity instanceof LivingEntity && entity.equals(Minecraft.getInstance().player))
        {
            float throttle = VehicleHelper.getThrottle((LivingEntity) entity);
            if(throttle != this.getThrottle())
            {
                this.setThrottle(throttle);
                PacketHandler.getPlayChannel().sendToServer(new MessageThrottle(throttle));
            }

            boolean handbraking = VehicleHelper.isHandbraking();
            if(this.isHandbraking() != handbraking)
            {
                this.setHandbraking(handbraking);
                PacketHandler.getPlayChannel().sendToServer(new MessageHandbrake(handbraking));
            }

            if(this.hasHorn())
            {
                boolean horn = VehicleHelper.isHonking();
                this.setHorn(horn);
                PacketHandler.getPlayChannel().sendToServer(new MessageHorn(horn));
            }

            float steeringAngle = VehicleHelper.getSteeringAngle(this);
            this.setSteeringAngle(steeringAngle);
            PacketHandler.getPlayChannel().sendToServer(new MessageTurnAngle(steeringAngle));
        }

        VehicleHelper.tryPlayEngineSound(this, this.level().getRandom());

        if(this.getHorn())
        {
            VehicleHelper.tryPlayHornSound(this, this.level().getRandom());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);

        if(compound.contains("owner", Tag.TAG_COMPOUND))
        {
            this.owner = compound.getUUID("owner");
        }

        this.setEngineStack(ItemStack.of(compound.getCompound("engineStack")));

        this.setStepHeight(compound.getFloat("stepHeight"));
        this.setCurrentEnergy(compound.getFloat("currentFuel"));

        this.setKeyNeeded(compound.getBoolean("keyNeeded"));
        this.setKeyStack(CommonUtils.readItemStackFromTag(compound, "keyStack"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);

        if(this.owner != null)
        {
            compound.putUUID("owner", this.owner);
        }

        compound.putBoolean("hasEngine", this.hasEngine());
        CommonUtils.writeItemStackToTag(compound, "engineStack", this.getEngineStack());

        compound.putFloat("stepHeight", this.getStepHeight());

        compound.putFloat("currentFuel", this.getCurrentEnergy());
        compound.putFloat("fuelCapacity", this.getEnergyCapacity());

        compound.putBoolean("keyNeeded", this.isKeyNeeded());
        CommonUtils.writeItemStackToTag(compound, "keyStack", this.getKeyStack());
    }

    @Nullable
    public LivingEntity getControllingPassenger()
    {
        if(this.getPassengers().isEmpty())
        {
            return null;
        }
        VehicleProperties properties = this.getProperties();
        for(Entity passenger : this.getPassengers())
        {
            int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
            if(seatIndex != -1 && properties.getSeats().get(seatIndex).isDriver())
            {
                return (LivingEntity) passenger;
            }
        }
        return null;
    }

    public boolean isMoving()
    {
        return this.motion.length() != 0;
    }

    //TODO remove these




    public void setSteeringAngle(float steeringAngle)
    {
        this.steeringAngle.set(this, steeringAngle);
    }

    public float getSteeringAngle()
    {
        return this.steeringAngle.get(this);
    }

    public void setThrottle(float power)
    {
        this.throttle.set(this, Mth.clamp(power, -1.0F, 1.0F));
    }

    public float getThrottle()
    {
        return this.throttle.get(this);
    }

    public final float getMaxSteeringAngle()
    {
        return this.getPoweredProperties().getMaxSteeringAngle();
    }

    public boolean hasEngine()
    {
        return !this.getEngineStack().isEmpty();
    }

    public void setEngineStack(ItemStack engine)
    {
        this.entityData.set(ENGINE_STACK, engine);
    }

    public ItemStack getEngineStack()
    {
        return this.entityData.get(ENGINE_STACK);
    }

    public Optional<IEngineTier> getEngineTier()
    {
        return IEngineTier.fromStack(this.getEngineStack());
    }

    @OnlyIn(Dist.CLIENT)
    public final boolean shouldRenderEngine()
    {
        return this.getPoweredProperties().isRenderEngine();
    }

    //@OnlyIn(Dist.CLIENT)
    public boolean shouldRenderFuelPort()
    {
        return true;
    }

    public final Vec3 getExhaustFumesPosition()
    {
        return this.getPoweredProperties().getExhaustFumesPosition();
    }

    public final boolean shouldShowExhaustFumes()
    {
        return this.getPoweredProperties().showExhaustFumes();
    }

    public void setHorn(boolean activated)
    {
        if(this.hasHorn())
        {
            this.entityData.set(HORN, activated);
        }
    }

    public boolean getHorn()
    {
        return this.entityData.get(HORN);
    }

    public void setBoosting(boolean boosting)
    {
        this.boosting = boosting;
        this.boostTimer = 10;
    }

    public boolean isBoosting()
    {
        return boosting;
    }

    public void setLaunching(int hold)
    {
        this.launching = true;
        this.launchingTimer = hold;
        this.disableFallDamage = true;
    }

    public boolean isLaunching()
    {
        return launching;
    }

    public boolean requiresEnergy()
    {
        return this.getPoweredProperties().requiresEnergy() && Config.SERVER.fuelEnabled.get();
    }



    public boolean isFueled()
    {
        return !this.requiresEnergy() || !Config.SERVER.fuelEnabled.get() || this.isControllingPassengerCreative() || this.getCurrentEnergy() > 0F;
    }

    public void setCurrentEnergy(float fuel)
    {
        this.entityData.set(CURRENT_FUEL, fuel);
    }

    public float getCurrentEnergy()
    {
        return this.entityData.get(CURRENT_FUEL);
    }

    public final float getEnergyCapacity()
    {
        return this.getPoweredProperties().getEnergyCapacity();
    }

    public final float getEnergyConsumptionPerTick()
    {
        return this.getPoweredProperties().getEnergyConsumptionPerTick();
    }

    public int addEnergy(int amount)
    {
        if(!this.requiresEnergy())
            return amount;
        float currentEnergy = this.getCurrentEnergy();
        currentEnergy += amount;
        int remaining = Math.max(0, Math.round(currentEnergy - this.getEnergyCapacity()));
        currentEnergy = Math.min(currentEnergy, this.getEnergyCapacity());
        this.setCurrentEnergy(currentEnergy);
        return remaining;
    }

    public void setKeyNeeded(boolean needsKey)
    {
        this.entityData.set(NEEDS_KEY, needsKey);
    }

    public boolean isKeyNeeded()
    {
        return this.entityData.get(NEEDS_KEY);
    }

    public void setKeyStack(ItemStack stack)
    {
        this.entityData.set(KEY_STACK, stack);
    }

    public ItemStack getKeyStack()
    {
        return this.entityData.get(KEY_STACK);
    }

    public void ejectKey()
    {
        if(!this.getKeyStack().isEmpty())
        {
            Vec3 keyHole = this.getWorldPosition(this.getIgnitionTransform(), 1.0F);
            this.level().addFreshEntity(new ItemEntity(this.level(), keyHole.x, keyHole.y, keyHole.z, this.getKeyStack()));
            this.setKeyStack(ItemStack.EMPTY);
        }
    }

    public final boolean isLockable()
    {
        return this.getPoweredProperties().canLockWithKey();
    }

    public boolean isEnginePowered()
    {
        return ((this.getEngineType() == EngineType.NONE || this.hasEngine()) && (this.isControllingPassengerCreative() || this.isFueled()) && this.getDestroyedStage() < 9) && (!this.isKeyNeeded() || !this.getKeyStack().isEmpty());
    }

    public boolean canDrive()
    {
        return (!this.canChangeWheels() || this.hasWheelStack()) && this.isEnginePowered();
    }

    public boolean isOwner(Player player)
    {
        return owner == null || player.getUUID().equals(owner);
    }

    public void setOwner(UUID owner)
    {
        this.owner = owner;
    }

    public void setHandbraking(boolean handbraking)
    {
        this.handbrake.set(this, handbraking);
    }

    public boolean isHandbraking()
    {
        return this.handbrake.get(this);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key)
    {
        super.onSyncedDataUpdated(key);

        if(level().isClientSide)
        {
            if(COLOR.equals(key))
            {
                /*Color color = new Color(this.dataManager.get(COLOR)); //TODO move this code to renderer to make fuel port darker or lighter
                int colorInt = (Math.sqrt(color.getRed() * color.getRed() * 0.241
                        + color.getGreen() * color.getGreen() * 0.691
                        + color.getBlue() * color.getBlue() * 0.068) > 127 ? color.darker() : color.brighter()).getRGB();*/
            }
        }
    }

    @Override
    public boolean causeFallDamage(float p_146828_, float distance, @NotNull DamageSource p_146830_)
    {
        if(this.disableFallDamage)
        {
            return false;
        }
        if(this.launchingTimer <= 0 && distance > 3)
        {
            this.disableFallDamage = false;
        }
        return super.causeFallDamage(p_146828_, distance, p_146830_);
    }

    private boolean isControllingPassengerCreative()
    {
        Entity entity = this.getControllingPassenger();
        if(entity instanceof Player)
        {
            return ((Player) entity).isCreative();
        }
        return false;
    }

    private void openEditInventory(Player player)
    {
        if(player instanceof ServerPlayer)
        {
            NetworkHooks.openScreen((ServerPlayer) player, this, buffer -> buffer.writeInt(this.getId()));
        }
    }

    public SimpleContainer getVehicleInventory()
    {
        if(this.vehicleInventory == null)
        {
            this.initVehicleInventory();
        }
        return this.vehicleInventory;
    }

    protected void initVehicleInventory()
    {
        this.vehicleInventory = new SimpleContainer(2);

        ItemStack engine = this.getEngineStack();
        if(this.getEngineType() != EngineType.NONE & !engine.isEmpty())
        {
            this.vehicleInventory.setItem(0, engine.copy());
        }

        ItemStack wheel = this.getWheelStack();
        if(this.canChangeWheels() && !wheel.isEmpty())
        {
            this.vehicleInventory.setItem(1, wheel.copy());
        }

        this.vehicleInventory.addListener(this);
    }

    private void updateSlots()
    {
        if(!this.level().isClientSide())
        {
            ItemStack engine = this.vehicleInventory.getItem(0);
            if(engine.getItem() instanceof EngineItem item)
            {
                if(item.getEngineType() == this.getEngineType())
                {
                    this.setEngineStack(engine.copy());
                }
                else
                {
                    this.setEngineStack(ItemStack.EMPTY);
                }
            }
            else if(this.getEngineType() != EngineType.NONE)
            {
                this.setEngineStack(ItemStack.EMPTY);
            }

            ItemStack wheel = this.vehicleInventory.getItem(1);
            if(this.canChangeWheels())
            {
                if(wheel.getItem() instanceof WheelItem)
                {
                    if(!this.hasWheelStack())
                    {
                        this.level().playSound(null, this.blockPosition(), ModSounds.BLOCK_JACK_AIR_WRENCH_GUN.get(), SoundSource.BLOCKS, 1.0F, 1.1F);
                        this.setWheelStack(wheel.copy());
                    }
                }
                else
                {
                    this.level().playSound(null, this.blockPosition(), ModSounds.BLOCK_JACK_AIR_WRENCH_GUN.get(), SoundSource.BLOCKS, 1.0F, 0.8F);
                    this.setWheelStack(ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void containerChanged(Container inventory)
    {
        this.updateSlots();
    }


    @Override
    protected void onVehicleDestroyed(LivingEntity entity)
    {
        super.onVehicleDestroyed(entity);
        boolean isCreativeMode = entity instanceof Player && ((Player) entity).isCreative();

        if(!isCreativeMode && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            // Spawns the engine if the vehicle has one
            ItemStack engine = this.getEngineStack();
            if(this.getEngineType() != EngineType.NONE && !engine.isEmpty())
            {
                InventoryUtil.spawnItemStack(this.level(), this.getX(), this.getY(), this.getZ(), engine);
            }

            // Spawns the key and removes the associated vehicle uuid
            ItemStack key = this.getKeyStack().copy();
            if(!key.isEmpty())
            {
                key.getOrCreateTag().remove("vehicleId");
                InventoryUtil.spawnItemStack(this.level(), this.getX(), this.getY(), this.getZ(), key);
            }

            // Spawns wheels if the vehicle has any
            ItemStack wheel = this.getWheelStack();
            if(this.canChangeWheels() && !wheel.isEmpty())
            {
                InventoryUtil.spawnItemStack(this.level(), this.getX(), this.getY(), this.getZ(), wheel.copy());
            }
        }
    }

    private void updateWheelPositions()
    {
        VehicleProperties properties = this.getProperties();
        if(properties.getWheels() != null)
        {
            List<Wheel> wheels = properties.getWheels();

            // Fixes game crashing if adding wheels when reloading vehicle properties json
           /* if(this.wheelPositions.length != wheels.size() * 3)
            {
                this.wheelPositions = new double[wheels.size() * 3];
            }*/

            double[] wheelPositions = this.getWheelPositions();
            for(int i = 0; i < wheels.size(); i++)
            {
                Wheel wheel = wheels.get(i);

                Transform bodyPosition = properties.getBodyTransform();
                double wheelX = bodyPosition.getX();
                double wheelY = bodyPosition.getY();
                double wheelZ = bodyPosition.getZ();

                double scale = bodyPosition.getScale();

                /* Applies axle and wheel offsets */
                wheelY += (properties.getWheelOffset() * 0.0625F) * scale;

                /* Wheels Translations */
                wheelX += ((wheel.getOffsetX() * 0.0625) * wheel.getSide().getOffset()) * scale;
                wheelY += (wheel.getOffsetY() * 0.0625) * scale;
                wheelZ += (wheel.getOffsetZ() * 0.0625) * scale;
                wheelX += ((((wheel.getWidth() * wheel.getScaleX()) / 2) * 0.0625) * wheel.getSide().getOffset()) * scale;

                /* Offsets the position to the wheel contact on the ground */
                wheelY -= ((8 * 0.0625) / 2.0) * scale * wheel.getScaleY();

                /* Update the wheel position */
                Vec3 wheelVec = new Vec3(wheelX, wheelY, wheelZ).yRot(-this.getYRot() * 0.017453292F);
                wheelPositions[i * 3] = wheelVec.x;
                wheelPositions[i * 3 + 1] = wheelVec.y;
                wheelPositions[i * 3 + 2] = wheelVec.z;
            }
        }
    }

//    protected void updateGroundState()
//    {
//        if(this.hasWheelStack())
//        {
//            VehicleProperties properties = this.getProperties();
//            List<Wheel> wheels = properties.getWheels();
//            if(this.hasWheelStack() && wheels != null)
//            {
//                for(int i = 0; i < wheels.size(); i++)
//                {
//                    double wheelX = this.wheelPositions[i * 3];
//                    double wheelY = this.wheelPositions[i * 3 + 1];
//                    double wheelZ = this.wheelPositions[i * 3 + 2];
//                    int x = Mth.floor(this.getX() + wheelX);
//                    int y = Mth.floor(this.getY() + wheelY - 0.2D);
//                    int z = Mth.floor(this.getZ() + wheelZ);
//                    BlockPos pos = new BlockPos(x, y, z);
//                    BlockState state = this.level.getBlockState(pos);
//                    if(!state.getCollisionShape(this.level, pos).isEmpty())
//                    {
//                        wheelsOnGround = true;
//                        return;
//                    }
//                }
//            }
//            wheelsOnGround = false;
//        }
//    }

    protected void releaseCharge(float strength)
    {
        this.boosting = true;
        this.boostStrength = Mth.clamp(strength, 0.0F, 1.0F);
        this.boostTimer = (int) (20 * this.boostStrength);
        this.speedMultiplier = 0.5F * this.boostStrength;
    }

    @Override
    public ItemStack getPickedResult(HitResult target)
    {
        ItemStack engine = ItemStack.EMPTY;
        if(this.hasEngine())
        {
            engine = this.getEngineStack();
        }

        ItemStack wheel = ItemStack.EMPTY;
        if(this.hasWheelStack())
        {
            wheel = this.getWheelStack();
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        if(entityId != null)
        {
            return VehicleCrateBlock.create(entityId, this.getColor(), engine, wheel);
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public Component getDisplayName()
    {
        return this.getName();
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new EditVehicleContainer(windowId, this.getVehicleInventory(), this, player, inventory);
    }

    public double[] getWheelPositions()
    {
        /* Updates the wheel positions as reloading vehicle properties
         * could cause a crash if wheels are added or removed. */
        if(this.wheelPositions == null || this.wheelPositions.length != this.getProperties().getWheels().size() * 3)
        {
            this.wheelPositions = new double[this.getProperties().getWheels().size() * 3];
        }
        return this.wheelPositions;
    }

    public float getBoostStrength()
    {
        return this.boostStrength;
    }

    public void setSpeedMultiplier(float speedMultiplier)
    {
        this.speedMultiplier = speedMultiplier;
    }

    public final IEngineType getEngineType()
    {
        return this.getPoweredProperties().getEngineType();
    }

    public final float getEnginePower()
    {
        return this.getPoweredProperties().getEnginePower();
    }

    public final Transform getIgnitionTransform()
    {
        return this.getPoweredProperties().getIgnitionTransform();
    }

    public final Vec3 getFrontAxleOffset()
    {
        return this.getPoweredProperties().getFrontAxleOffset();
    }

    public final Vec3 getRearAxleOffset()
    {
        return this.getPoweredProperties().getRearAxleOffset();
    }

    public final boolean hasHorn()
    {
        return this.getPoweredProperties().hasHorn();
    }

    protected final PoweredProperties getPoweredProperties()
    {
        return this.getProperties().getExtended(PoweredProperties.class);
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateEngineSound()
    {
        if(this.charging)
        {
            this.enginePitch = this.getMinEnginePitch() + (this.getMaxEnginePitch() - this.getMinEnginePitch()) * 0.75F * this.chargingAmount;
            return;
        }

        this.enginePitch = this.getMinEnginePitch() + (this.getMaxEnginePitch() - this.getMinEnginePitch()) * (float) Math.abs(this.getSpeed() / 25F);
        this.engineVolume = this.getControllingPassenger() != null && this.isEnginePowered() ? 1.0F : 0.001F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getEnginePitch()
    {
        return this.enginePitch;
    }

    @OnlyIn(Dist.CLIENT)
    public float getEngineVolume()
    {
        return this.engineVolume;
    }

    @OnlyIn(Dist.CLIENT)
    public float getRenderWheelAngle(float partialTicks)
    {
        return this.prevRenderWheelAngle + (this.renderWheelAngle - this.prevRenderWheelAngle) * partialTicks;
    }

    public void setStepHeight(float stepHeight)
    {
        this.entityData.set(STEP_HEIGHT, stepHeight);
    }

    @Override
    public float getStepHeight()
    {
        return this.entityData.get(STEP_HEIGHT);
    }
}
