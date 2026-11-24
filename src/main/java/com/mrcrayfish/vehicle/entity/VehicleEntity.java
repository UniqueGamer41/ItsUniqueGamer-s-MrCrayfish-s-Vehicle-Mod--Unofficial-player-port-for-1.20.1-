package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.framework.FrameworkForge;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.data.CosmeticRayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import com.mrcrayfish.vehicle.common.CosmeticTracker;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.SeatTracker;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticActions;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncActionData;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.*;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipe;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipes;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.network.datasync.VehicleDataValue;
import com.mrcrayfish.vehicle.util.CommonUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static com.mrcrayfish.vehicle.entity.PoweredVehicleEntity.CURRENT_SPEED;
import static com.mrcrayfish.vehicle.entity.PoweredVehicleEntity.MAX_SPEED;

/**
 * Author: MrCrayfish
 */
public abstract class VehicleEntity extends Entity implements IEntityAdditionalSpawnData
{
    public static final int[] DYE_TO_COLOR = new int[] {16383998, 16351261, 13061821, 3847130, 16701501, 8439583, 15961002, 4673362, 10329495, 1481884, 8991416, 3949738, 8606770, 6192150, 11546150, 1908001};

    protected static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> TIME_SINCE_HIT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> TRAILER = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<ItemStack> WHEEL_STACK = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.ITEM_STACK);

    protected UUID trailerId;
    protected TrailerEntity trailer = null;
    private int searchDelay = 0;

    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYaw;
    protected double lerpPitch;

    protected final SeatTracker seatTracker;
    protected final CosmeticTracker cosmeticTracker;
    protected final Map<EntityDataAccessor<?>, VehicleDataValue<?>> paramToDataValue = new Object2ObjectOpenHashMap<>();

    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationPitch, bodyRotationPitch;

    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationYaw, bodyRotationYaw;

    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationRoll, bodyRotationRoll;

    @OnlyIn(Dist.CLIENT)
    protected float passengerYawOffset;

    @OnlyIn(Dist.CLIENT)
    protected float passengerPitchOffset;

    public VehicleEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.seatTracker = new SeatTracker(this);
        this.cosmeticTracker = new CosmeticTracker(this);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(TIME_SINCE_HIT, 0);
        this.entityData.define(HEALTH, 100F);
        this.entityData.define(COLOR, 16383998);
        this.entityData.define(TRAILER, -1);
        this.entityData.define(WHEEL_STACK, ItemStack.EMPTY);
    }

    public void registerDataValue(VehicleDataValue<?> dataValue)
    {
        this.paramToDataValue.put(dataValue.getKey(), dataValue);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key)
    {
        super.onSyncedDataUpdated(key);

        Entity entity = this.getControllingPassenger();
        if(entity != null)
        {
            if(entity instanceof Player)
            {
                if(!((Player) entity).isLocalPlayer())
                {
                    VehicleDataValue<?> value = this.paramToDataValue.get(key);

                    if(value != null)
                    {
                        value.updateLocal(this);
                    }
                }
            }
        }
    }

    /* Overridden to prevent odd step sound when driving vehicles. Ain't no subclasses getting
     * the ability to override this. */
    @Override
    protected final void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state)
    {}

    @Override
    @NotNull
    public AABB getBoundingBoxForCulling()
    {
        return this.getBoundingBox().inflate(1);
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

//    @Override
//    @NotNull
//    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
//    {
//        if(this.level().isClientSide || player.isCrouching())
//        {
//            return InteractionResult.SUCCESS;
//        }
//
//        int trailerId = SyncedEntityData.instance().get(player, ModDataKeys.TRAILER);
//        if(trailerId != -1)
//        {
//            if(this.getVehicle() == null && this.canTowTrailers() && this.getTrailer() == null)
//            {
//                Entity entity = this.level().getEntity(trailerId);
//                if(entity instanceof TrailerEntity trailer && entity != this)
//                {
//                    this.setTrailer(trailer);
//                    SyncedEntityData.instance().set(player, ModDataKeys.TRAILER, -1);
//                }
//            }
//            return InteractionResult.SUCCESS;
//        }
//
//        ItemStack heldItem = player.getItemInHand(hand);
//
//        if(heldItem.getItem() instanceof SprayCanItem)
//        {
//            if(this.getProperties().canBePainted())
//            {
//                CompoundTag compound = heldItem.getTag();
//                if(compound != null)
//                {
//                    if(!compound.contains(SprayCanItem.REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
//                    {
//                        compound.putInt(SprayCanItem.REMAINING_SPRAYS_TAG, ModItems.SPRAY_CAN.get().getCapacity(heldItem));
//                    }
//
//                    int remainingSprays = compound.getInt(SprayCanItem.REMAINING_SPRAYS_TAG);
//                    if(compound.contains(IDyeable.NBT_KEY, IntTag.TAG_INT) && remainingSprays > 0)
//                    {
//                        int color = compound.getInt(IDyeable.NBT_KEY);
//                        if(this.getColor() != color)
//                        {
//                            this.setColor(compound.getInt(IDyeable.NBT_KEY));
//                            player.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ITEM_SPRAY_CAN_SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
//                            compound.putInt(SprayCanItem.REMAINING_SPRAYS_TAG, remainingSprays - 1);
//                        }
//                    }
//                }
//            }
//            return InteractionResult.SUCCESS;
//        }
//        else if(heldItem.getItem() == ModItems.HAMMER.get() && this.getVehicle() instanceof EntityJack)
//        {
//            if(this.getHealth() < this.getMaxHealth())
//            {
//                heldItem.hurtAndBreak(1, player, playerEntity -> player.broadcastBreakEvent(hand));
//                this.setHealth(this.getHealth() + 5F);
//                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_THUD.get(), SoundSource.PLAYERS, 1.0F, 0.8F + 0.4F * random.nextFloat());
//                player.swing(hand);
//                if(player instanceof ServerPlayer)
//                {
//                    ((ServerPlayer) player).connection.send(new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
//                }
//                if(this.getHealth() == this.getMaxHealth())
//                {
//                    if(level() instanceof ServerLevel)
//                    {
//                        //TODO send as single packet instead of multiple
//                        int count = (int) (50 * (this.getBbWidth() * this.getBbHeight()));
//                        for(int i = 0; i < count; i++)
//                        {
//                            double width = this.getBbWidth() * 2;
//                            double height = this.getBbHeight() * 1.5;
//
//                            Vec3 heldOffset = this.getProperties().getHeldOffset().yRot((float) Math.toRadians(-this.getYRot()));
//                            double x = this.getX() + width * random.nextFloat() - width / 2 + heldOffset.z * 0.0625;
//                            double y = this.getY() + height * random.nextFloat();
//                            double z = this.getZ() + width * random.nextFloat() - width / 2 + heldOffset.x * 0.0625;
//
//                            double d0 = random.nextGaussian() * 0.02D;
//                            double d1 = random.nextGaussian() * 0.02D;
//                            double d2 = random.nextGaussian() * 0.02D;
//                            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, d0, d1, d2, 1.0);
//                        }
//                    }
//                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
//                }
//            }
//            return InteractionResult.SUCCESS;
//        }
//        else if(this.canRide(player))
//        {
//            int seatIndex = this.seatTracker.getClosestAvailableSeatToPlayer(player);
//            if(seatIndex != -1)
//            {
//                if(player.startRiding(this))
//                {
//                    this.getSeatTracker().setSeatIndex(seatIndex, player.getUUID());
//                    this.onPlayerChangeSeat(player, -1, seatIndex);
//                }
//            }
//            return InteractionResult.SUCCESS;
//        }
//
//        return super.interact(player, hand);
//    } //TODO CHECK NEW VS OLD INTERACT METHOD

    public void syncAction(ResourceLocation cosmeticId, Action action) {
        if (!this.level().isClientSide()) {
            CompoundTag tag = action.save(true);
            MessageSyncActionData msg = new MessageSyncActionData(this.getId(), cosmeticId,
                    List.of(Pair.of(CosmeticActions.getId(action.getClass()), tag)));
            PacketHandler.getPlayChannel().sendToTracking(() -> this, msg);

        }
    }

    public Vec3 getHitchPosition()
    {
        Vec3 offset = this.getProperties().getTowBarOffset();

        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();

        return this.position()
                .add(forward.scale(offset.z))
                .add(right.scale(offset.x))
                .add(0, offset.y, 0);
    }

    @Override
    @NotNull
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
    {
        //System.out.println("[DEBUG] INTERACTION METHOD TRIGGERED");

        //TRAILERS
        if(!this.level().isClientSide() && !player.isCrouching()){
            int trailerId = SyncedEntityData.instance().get(player, ModDataKeys.TRAILER);
            if(trailerId != -1){
                System.out.println("[DEBUG] Trailer ID: " + trailerId);
                if(!this.canTowTrailers()){
                    CommonUtils.sendInfoMessage(player, "vehicle.status.cannot_tow_trailers");
                }
                if(this.getVehicle() == null && this.canTowTrailers() && this.getTrailer() == null){
                    Entity entity = this.level().getEntity(trailerId);
                    System.out.println("[DEBUG] ENTITY DETAILS:" + entity);
                    if(entity instanceof TrailerEntity && entity != this){
                        TrailerEntity trailer = (TrailerEntity) entity;
                        this.setTrailer(trailer);
                        SyncedEntityData.instance().set(player, ModDataKeys.TRAILER, -1);

                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // SPRAY CAN CHECK
        if(heldItem.getItem() instanceof SprayCanItem)
        {
            if(this.getProperties().canBePainted())
            {
                CompoundTag compound = heldItem.getTag();
                if(compound != null)
                {
                    if(!compound.contains(SprayCanItem.REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
                    {
                        compound.putInt(SprayCanItem.REMAINING_SPRAYS_TAG, ModItems.SPRAY_CAN.get().getCapacity(heldItem));
                    }

                    int remainingSprays = compound.getInt(SprayCanItem.REMAINING_SPRAYS_TAG);
                    if(compound.contains(IDyeable.NBT_KEY, IntTag.TAG_INT) && remainingSprays > 0)
                    {
                        int color = compound.getInt(IDyeable.NBT_KEY);
                        if(this.getColor() != color)
                        {
                            this.setColor(compound.getInt(IDyeable.NBT_KEY));
                            player.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ITEM_SPRAY_CAN_SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            compound.putInt(SprayCanItem.REMAINING_SPRAYS_TAG, remainingSprays - 1);
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // HAMMER CHECK
        else if(heldItem.getItem() == ModItems.HAMMER.get() && this.getVehicle() instanceof EntityJack)
        {
            if(this.getHealth() < this.getMaxHealth())
            {
                heldItem.hurtAndBreak(1, player, playerEntity -> player.broadcastBreakEvent(hand));
                this.setHealth(this.getHealth() + 5F);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_THUD.get(), SoundSource.PLAYERS, 1.0F, 0.8F + 0.4F * random.nextFloat());
                player.swing(hand);
                if(player instanceof ServerPlayer)
                {
                    ((ServerPlayer) player).connection.send(new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
                }
                if(this.getHealth() == this.getMaxHealth())
                {
                    if(level() instanceof ServerLevel)
                    {
                        int count = (int) (50 * (this.getBbWidth() * this.getBbHeight()));
                        for(int i = 0; i < count; i++)
                        {
                            double width = this.getBbWidth() * 2;
                            double height = this.getBbHeight() * 1.5;

                            Vec3 heldOffset = this.getProperties().getHeldOffset().yRot((float) Math.toRadians(-this.getYRot()));
                            double x = this.getX() + width * random.nextFloat() - width / 2 + heldOffset.z * 0.0625;
                            double y = this.getY() + height * random.nextFloat();
                            double z = this.getZ() + width * random.nextFloat() - width / 2 + heldOffset.x * 0.0625;

                            double d0 = random.nextGaussian() * 0.02D;
                            double d1 = random.nextGaussian() * 0.02D;
                            double d2 = random.nextGaussian() * 0.02D;
                            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, d0, d1, d2, 1.0);
                        }
                    }
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // MOUNTING CHECK - ONLY after all other interactions have been checked
        else if(this.canRide(player))
        {
            int seatIndex = this.seatTracker.getClosestAvailableSeatToPlayer(player);
            if(seatIndex != -1)
            {
                if(player.startRiding(this))
                {
                    this.getSeatTracker().setSeatIndex(seatIndex, player.getUUID());
                    this.onPlayerChangeSeat(player, -1, seatIndex);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.interact(player, hand);
    }


    @Override
    @NotNull
    public InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 hitVec, @NotNull InteractionHand hand)
    {
        if(player.isCrouching())
        {
            return InteractionResult.PASS;
        }

        // Client-side: just return success to prevent further processing
        if(this.level().isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        // Server-side: Find which cosmetic was clicked
        ResourceLocation cosmeticId = this.getClosestCosmeticToHit(hitVec);

        if(cosmeticId != null)
        {
            Collection<Action> actions = this.cosmeticTracker.getActions(cosmeticId);
            if(!actions.isEmpty())
            {
                // Trigger all actions for this cosmetic
                for(Action action : actions)
                {
                    action.onInteract(this, player);
                    this.syncAction(cosmeticId, action);
                }
                return InteractionResult.CONSUME;
            }
        }

        // Fall back to regular interact (mounting, etc.)
        return this.interact(player, hand);
    }

    @Nullable
    private ResourceLocation getClosestCosmeticToHit(Vec3 hitVec)
    {
        VehicleProperties properties = this.getProperties();

        // hitVec is already relative to entity position, just needs rotation and scaling
        Vec3 rotated = hitVec.yRot((float) Math.toRadians(this.getYRot()));

        // Scale to cosmetic coordinate system (1/16 block units)
        Vec3 localHit = new Vec3(rotated.x * 16.0, rotated.y * 16.0, rotated.z * 16.0);

        // Find closest cosmetic within range
        ResourceLocation closestId = null;
        double closestDist = 40.0; // Max distance threshold

        for(Map.Entry<ResourceLocation, com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties> entry : properties.getCosmetics().entrySet())
        {
            Vec3 offset = entry.getValue().getOffset();
            double dist = offset.distanceTo(localHit);

            if(dist < closestDist)
            {
                closestDist = dist;
                closestId = entry.getKey();
            }
        }

        return closestId;
    }


    /**
     * Registers cosmetic parts for ray tracing. Call this in your vehicle's static initialization
     * or in a setup method.
     */
    public static void registerCosmeticRayTrace(EntityType<? extends VehicleEntity> entityType, Supplier<RayTraceTransforms> transformsSupplier)
    {
        EntityRayTracer.instance().registerTransforms(entityType, transformsSupplier);
    }

    /**
     * Creates ray trace transforms for this vehicle's cosmetics
     */
    protected static RayTraceTransforms createCosmeticTransforms(EntityType<? extends VehicleEntity> entityType)
    {
        return new RayTraceTransforms() {
            @Override
            public void load(EntityRayTracer tracer, List<MatrixTransform> transforms, HashMap<RayTraceData, List<MatrixTransform>> parts) {
                VehicleProperties properties = VehicleProperties.get(entityType);
                if(properties == null)
                    return;

                properties.getCosmetics().forEach(((cosmeticId, cosmeticProperties) -> {
                    if(!cosmeticProperties.getModelLocations().isEmpty()) {
                        ResourceLocation modelLocation = cosmeticProperties.getModelLocations().get(0);
                        ComponentModel model = ComponentManager.lookupModel(modelLocation);

                        if(model != null && model.getBaseModel() != null) {
                            Vec3 offset = cosmeticProperties.getOffset();

                            List<MatrixTransform> cosmeticTransforms = new ObjectArrayList<>(transforms);
                            cosmeticTransforms.add(MatrixTransform.translate((float) offset.x, (float) offset.y, (float) offset.z));
                            cosmeticTransforms.add(MatrixTransform.scale(0.0625F));

                            CosmeticRayTraceData data = new CosmeticRayTraceData(cosmeticId, (ResourceLocation) model.getBaseModel(), null);
                            parts.put(data, cosmeticTransforms);
                        }
                    }
                }));
            }
        };
    }





    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if(compound.contains(IDyeable.NBT_KEY, IntArrayTag.TAG_INT_ARRAY))
        {
            int[] c = compound.getIntArray(IDyeable.NBT_KEY);
            if(c.length == 3)
            {
                int color = ((c[0] & 0xFF) << 16) | ((c[1] & 0xFF) << 8) | ((c[2] & 0xFF));
                this.setColor(color);
            }
        }
        if(compound.contains("health", FloatTag.TAG_FLOAT))
        {
            this.setHealth(compound.getFloat("health"));
        }
        if(compound.hasUUID("trailer"))
        {
            this.trailerId = compound.getUUID("trailer");
        }
        if(compound.contains("seatTracker", CompoundTag.TAG_COMPOUND))
        {
            this.seatTracker.read(compound.getCompound("seatTracker"));
        }
        if(compound.contains("cosmeticTracker", CompoundTag.TAG_COMPOUND))
        {
            this.cosmeticTracker.read(compound.getCompound("cosmeticTracker"));
        }
        if(compound.contains("wheelStack", CompoundTag.TAG_COMPOUND))
        {
            this.setWheelStack(ItemStack.of(compound.getCompound("wheelStack")));
        }
    }

//    @Override
//    protected void addAdditionalSaveData(CompoundTag compound)
//    {
//        compound.putIntArray(IDyeable.NBT_KEY, this.getColorRGB());
//        compound.putFloat("maxHealth", this.getMaxHealth());
//        compound.putFloat("health", this.getHealth());
//
//        //TODO make it save the entity
//        if(this.trailerId != null)
//        {
//            compound.putUUID("trailer", this.trailerId);
//        }
//
//        compound.put("seatTracker", this.seatTracker.write());
//        compound.put("cosmeticTracker", this.cosmeticTracker.write());
//        CommonUtils.writeItemStackToTag(compound, "wheelStack", this.getWheelStack());
//    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putIntArray(IDyeable.NBT_KEY, this.getColorRGB());
        compound.putFloat("maxHealth", this.getMaxHealth());
        compound.putFloat("health", this.getHealth());

        //TODO make it save the entity
        if(this.trailerId != null)
        {
            compound.putUUID("trailer", this.trailerId);
        }

        compound.put("seatTracker", this.seatTracker.write());
        compound.put("cosmeticTracker", this.cosmeticTracker.write());
        CommonUtils.writeItemStackToTag(compound, "wheelStack", this.getWheelStack());
    }

    @Override
    public void tick()
    {
        this.cosmeticTracker.tick(this);

        if(this.getTimeSinceHit() > 0)
        {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }

        if(!this.level().isClientSide())
        {
            if(this.searchDelay <= 0)
            {
                if(this.trailer != null)
                {
                    /* Updates periodically to ensure the client knows the vehicle/trailer connection.
                     * There is often problems on loading worlds that it doesn't sync correctly, so this
                     * is the fix. */
                    this.entityData.set(TRAILER, trailer.getId());
                    this.trailer.getEntityData().set(TrailerEntity.PULLING_ENTITY, this.getId());
                    this.searchDelay = Config.SERVER.trailerSyncCooldown.get();
                }
                else
                {
                    this.findTrailer();
                }
            }
            else
            {
                this.searchDelay--;
            }


        }

        if(this.level().isClientSide)
        {
            int entityId = this.entityData.get(TRAILER);
            if(entityId != -1)
            {
                Entity entity = this.level().getEntity(this.entityData.get(TRAILER));
                if(entity instanceof TrailerEntity)
                {
                    this.trailer = (TrailerEntity) entity;
                    this.trailerId = trailer.getUUID();
                }
                else if(this.trailer != null)
                {
                    this.trailer = null;
                    this.trailerId = null;
                }
            }
            else if(this.trailer != null)
            {
                this.trailer = null;
                this.trailerId = null;
            }
        }

        if(!this.level().isClientSide && this.trailer != null && (!this.trailer.isAlive() || this.trailer.getPullingEntity() != this))
        {
            this.setTrailer(null);
        }

        super.tick();
        this.tickLerp();
        this.onUpdateVehicle();

        if(this.level().isClientSide())
        {
            this.prevBodyRotationPitch = this.bodyRotationPitch;
            this.prevBodyRotationYaw = this.bodyRotationYaw;
            this.prevBodyRotationRoll = this.bodyRotationRoll;

            this.updateBodyRotations();
            this.updateWheelRotations();

            while(this.bodyRotationYaw - this.prevBodyRotationYaw < -180F)
            {
                this.prevBodyRotationYaw -= 360F;
            }

            while(this.bodyRotationYaw - this.prevBodyRotationYaw >= 180F)
            {
                this.prevBodyRotationYaw += 360F;
            }

            while(this.bodyRotationRoll - this.prevBodyRotationRoll < -180F)
            {
                this.prevBodyRotationRoll -= 360F;
            }

            while(this.bodyRotationRoll - this.prevBodyRotationRoll >= 180F)
            {
                this.prevBodyRotationRoll += 360F;
            }
        }
    }

    private void findTrailer()
    {
        if(!this.level().isClientSide && this.trailerId != null && this.trailer == null)
        {
            ServerLevel server = (ServerLevel) this.level();
            Entity entity = server.getEntity(this.trailerId);
            if(entity instanceof TrailerEntity)
            {
                this.setTrailer((TrailerEntity) entity);
                return;
            }
            this.trailerId = null;
        }
    }

//    @Override
//    public void remove(RemovalReason reason)
//    {
//        System.out.println(
//                "[VehicleEntity] REMOVE id=" + this.getId()
//                        + " reason=" + reason
//                        + " client=" + this.level().isClientSide()
//        );
//
//        super.remove(reason);
//    }

    protected abstract void onUpdateVehicle();

//    @Override
//    public boolean hurt(@NotNull DamageSource source, float amount)
//    {
//        if(this.isInvulnerableTo(source))
//        {
//            return false;
//        }
//
//        if(this.level().isClientSide() || !this.isAlive())
//        {
//            return true;
//        }
//
//        Entity trueSource = source.getEntity();
//
//        if(source.getEntity() != null
//                && source.getDirectEntity() != null
//                && this.hasPassenger(trueSource))
//        {
//            return false;
//        }
//
//        float healthBefore = this.getHealth();
//
//        if(Config.SERVER.vehicleDamage.get())
//        {
//            this.setTimeSinceHit(10);
//
//            boolean lethalHit = healthBefore - amount <= 0.0F;
//
//            this.setHealth(healthBefore - amount);
//
//            if(lethalHit)
//            {
//                if(Config.SERVER.useLegacyDamageSystem.get())
//                {
//                    this.onVehicleDestroyed((LivingEntity) trueSource);
//                    this.remove(RemovalReason.KILLED);
//                }
//
//                // When legacy mode is false, deliberately leave the entity
//                // at 0 health so it remains visible but becomes inoperable.
//            }
//        }
//
//        return true;
//    } //TODO TEST NEW VS OLD SYSTEMS

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount)
    {
        if(this.isInvulnerableTo(source))
        {
            return false;
        }
        else if(!this.level().isClientSide() && this.isAlive()) {
            Entity trueSource = source.getEntity();
            if (source.getEntity() != null && source.getDirectEntity() != null && this.hasPassenger(trueSource)) {
                return false;
            } else {
                if (Config.SERVER.vehicleDamage.get()) {
                    this.setTimeSinceHit(10);
                    this.setHealth(this.getHealth() - amount);
                }
                if (Config.SERVER.useLegacyDamageSystem.get()) {
                    boolean isCreativeMode = trueSource instanceof Player && ((Player) trueSource).isCreative();
                    if (isCreativeMode || this.getHealth() == 0.0F) {
                        this.onVehicleDestroyed((LivingEntity) trueSource);
                        this.remove(RemovalReason.KILLED);
                    }

                    return true;
                }
                else {
                    boolean isCreativeMode = trueSource instanceof Player && ((Player) trueSource).isCreative();
                    if (isCreativeMode || this.getHealth() < 0.0F) {
                        this.onVehicleDestroyed((LivingEntity) trueSource);
                        this.remove(RemovalReason.KILLED);
                    }

                    return true;

                }
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean causeFallDamage(float amount, float distance, @NotNull DamageSource source)
    {
        if(Config.SERVER.vehicleDamage.get() && !this.immuneToFallDamage() && distance >= 4F && this.getDeltaMovement().y() < -1.0F)
        {
            float damage = distance / 2F;
            this.hurt(this.level().damageSources().fall(), damage);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_IMPACT.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        }
        return true;
    }

    protected void onVehicleDestroyed(LivingEntity entity)
    {
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.ENTITY_VEHICLE_DESTROYED.get(),
                SoundSource.AMBIENT,
                1.0F,
                0.5F
        );

        if(Config.SERVER.useLegacyDamageSystem.get()) {
            System.out.println("Using Legacy System for damage...");

            boolean isCreativeMode =
                    entity instanceof Player && ((Player) entity).isCreative();

            if (!isCreativeMode
                    && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS) && Config.SERVER.useLegacyDamageSystem.get() == true) {
                WorkstationRecipe recipe =
                        WorkstationRecipes.getRecipe(this.getType(), this.level());

                if (recipe != null) {
                    for (WorkstationIngredient ingredient : recipe.getMaterials()) {
                        ItemStack[] matchingItems = ingredient.getItems();

                        // Missing/invalid external item
                        if (matchingItems.length == 0)
                            continue;

                        /*
                         * For a normal item ingredient this contains the actual item.
                         * For a tag ingredient it contains all matching items, so choose
                         * one of the valid items to return.
                         */
                        ItemStack copy =
                                matchingItems[this.random.nextInt(matchingItems.length)].copy();

                        copy.setCount(ingredient.getCount());

                        int shrink = copy.getCount() / 2;

                        if (shrink > 0) {
                            copy.shrink(this.random.nextInt(shrink + 1));
                        }

                        if (!copy.isEmpty()) {
                            InventoryUtil.spawnItemStack(
                                    this.level(),
                                    this.getX(),
                                    this.getY(),
                                    this.getZ(),
                                    copy
                            );
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Using the current system for vehicle damage...");
            return;
        }
    }

    public int getDestroyedStage()
    {
        return 10 - (int) Math.max(1.0F, (int) Math.ceil(10.0F * (this.getHealth() / this.getMaxHealth())));
    }

    /**
     * Smooths the rendering on servers
     */
    private void tickLerp()
    {
        if(this.isControlledByLocalInstance())
        {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if(this.lerpSteps > 0)
        {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double) this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double) this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double) this.lerpSteps;
            double d3 = Mth.wrapDegrees(this.lerpYaw - (double) this.getYRot());
            float yRot = (float) ((double) this.getYRot() + d3 / (double) this.lerpSteps);
            float xRot = (float) ((double) this.getXRot() + (this.lerpPitch - (double) this.getXRot()) / (double) this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d1, d2);
            this.setRot(yRot, xRot);
        }
    }


    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = (double) yaw;
        this.lerpPitch = (double) pitch;
        this.lerpSteps = 10;
    }

    @Override
    protected boolean canRide(@NotNull Entity entityIn)
    {
        return true;
    }

    @Override
    public void push(double x, double y, double z) {}

    /**
     * Sets the time to count down from since the last time entity was hit.
     */
    public void setTimeSinceHit(int timeSinceHit)
    {
        this.entityData.set(TIME_SINCE_HIT, timeSinceHit);
    }

    /**
     * Gets the time since the last hit.
     */
    public int getTimeSinceHit()
    {
        return this.entityData.get(TIME_SINCE_HIT);
    }

    /**
     * Gets the max health of the vehicle.
     */
    public final float getMaxHealth()
    {
        return this.getProperties().getMaxHealth();
    }

    /**
     * Sets the current health of the vehicle.
     */
    public void setHealth(float health)
    {
        this.entityData.set(HEALTH, Mth.clamp(health, 0F, this.getMaxHealth()));
    }

    /**
     * Gets the current health of the vehicle.
     */
    public float getHealth()
    {
        return this.entityData.get(HEALTH);
    }

    public boolean hasWheelStack()
    {
        return !this.getWheelStack().isEmpty();
    }

    public void setWheelStack(ItemStack wheels)
    {
        this.entityData.set(WHEEL_STACK, wheels);
    }

    public ItemStack getWheelStack()
    {
        return this.entityData.get(WHEEL_STACK);
    }

    public Optional<IWheelType> getWheelType()
    {
        return IWheelType.fromStack(this.entityData.get(WHEEL_STACK));
    }

    //TODO look into this and why its here. May have to send vanilla event to client
    //@Override
    @OnlyIn(Dist.CLIENT)
    public void animateHurt()
    {
        this.setTimeSinceHit(10);
    }

    public void setColor(int color)
    {
        if(this.getProperties().canBePainted())
        {
            this.entityData.set(COLOR, color);
        }
    }

    public void setColorRGB(int r, int g, int b)
    {
        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
        this.entityData.set(COLOR, color);
    }

    public int getColor()
    {
        return this.entityData.get(COLOR);
    }

    public int[] getColorRGB()
    {
        int color = this.entityData.get(COLOR);
        return new int[]{ (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF };
    }

    /**
     * Gets the absolute position of a part in the world
     *
     * @param position the position definition of the part
     * @return a Vector3d containing the exact location
     */
    public Vec3 getWorldPosition(Transform position, float partialTicks)
    {
        VehicleProperties properties = this.getProperties();
        Transform bodyPosition = properties.getBodyTransform();
        Vec3 partVec = Vec3.ZERO;
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.scale(position.getScale());
        partVec = partVec.add(0, -0.5, 0);
        partVec = partVec.add(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
        partVec = partVec.add(0, properties.getWheelOffset() * 0.0625, 0);
        partVec = partVec.add(0, properties.getAxleOffset() * 0.0625, 0);
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.scale(bodyPosition.getScale());
        partVec = partVec.add(0, -0.5, 0);
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.add(bodyPosition.getX(), bodyPosition.getY(), bodyPosition.getZ());
        partVec = partVec.yRot(-(this.yRotO + (this.getYRot() - this.yRotO) * partialTicks) * 0.017453292F);
        partVec = partVec.add(this.xo + (this.getX() - this.xo) * partialTicks, 0, 0);
        partVec = partVec.add(0, this.yo + (this.getY() - this.yo) * partialTicks, 0);
        partVec = partVec.add(0, 0, this.zo + (this.getZ() - this.zo) * partialTicks);
        return partVec;
    }

    protected static AABB createScaledBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, double scale)
    {
        return new AABB(x1 * scale, y1 * scale, z1 * scale, x2 * scale, y2 * scale, z2 * scale);
    }

    protected static AABB createBoxScaled(double x1, double y1, double z1, double x2, double y2, double z2, double scale)
    {
        return new AABB(x1 * 0.0625 * scale, y1 * 0.0625 * scale, z1 * 0.0625 * scale, x2 * 0.0625 * scale, y2 * 0.0625 * scale, z2 * 0.0625 * scale);
    }



    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeFloat(this.getYRot());
        this.seatTracker.write(buffer);
        this.cosmeticTracker.write(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.setYRot(this.yRotO = buffer.readFloat());
        this.seatTracker.read(buffer);
        this.cosmeticTracker.read(buffer);
    }

    public final boolean canTowTrailers()
    {
        return this.getProperties().canTowTrailers();
    }

    public void setTrailer(TrailerEntity trailer)
    {
        if(trailer != null)
        {
            this.trailer = trailer;
            this.trailerId = trailer.getUUID();
            trailer.setPullingEntity(this);
            this.entityData.set(TRAILER, trailer.getId());
        }
        else
        {
            if(this.trailer != null && this.trailer.getPullingEntity() == this)
            {
                this.trailer.setPullingEntity(null);
            }
            this.trailer = null;
            this.trailerId = null;
            this.entityData.set(TRAILER, -1);
        }
    }

    @Nullable
    public UUID getTrailerId()
    {
        return trailerId;
    }

    @Nullable
    public TrailerEntity getTrailer()
    {
        return trailer;
    }

    public boolean canChangeWheels()
    {
        return this.getProperties().canChangeWheels();
    }

    public final boolean immuneToFallDamage()
    {
        return this.getProperties().immuneToFallDamage();
    }

    public final boolean canPlayerCarry()
    {
        return this.getProperties().canPlayerCarry();
    }

    public final boolean canFitInTrailer()
    {
        return this.getProperties().canFitInTrailer();
    }

    public VehicleProperties getProperties()
    {
        return VehicleProperties.get(this.getType());
    }

    @Override
    public ItemStack getPickedResult(HitResult target)
    {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        if(entityId != null)
        {
            ItemStack wheel = ItemStack.EMPTY;
            if(this.hasWheelStack())
            {
                wheel = this.getWheelStack();
            }
            return VehicleCrateBlock.create(entityId, this.getColor(), null, wheel);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public CosmeticTracker getCosmeticTracker()
    {
        return this.cosmeticTracker;
    }

    public SeatTracker getSeatTracker()
    {
        return this.seatTracker;
    }

    /**
     * Called when the player mounts a seat, changes seat, and dismounts a seat. If the oldSeatIndex
     * is -1 then the player is mounting the vehicle. If the newSeatIndex is -1 then the player is
     * dismounting the vehicle.
     * @param player the player changing seat
     * @param oldSeatIndex the index of the seat the player was previously sitting on
     * @param newSeatIndex the index of the seat the player is now sitting on
     */
    public void onPlayerChangeSeat(Player player, int oldSeatIndex, int newSeatIndex)
    {
        if(newSeatIndex != -1 && this.level().isClientSide())
        {
            Seat seat = this.getProperties().getSeats().get(newSeatIndex);
            player.setYRot(this.yRot + seat.getYawOffset());
            player.setYHeadRot(player.getYRot());
            this.updatePassengerOffsets(player);
            this.updatePassengerPosition(player);
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger)
    {
        super.removePassenger(passenger);
        if(!this.level().isClientSide() && passenger instanceof Player)
        {
            int oldSeatIndex = this.seatTracker.getSeatIndex(passenger.getUUID());
            this.onPlayerChangeSeat((Player) passenger, oldSeatIndex, -1);
        }
    }

    @Override
    public void addPassenger(@NotNull Entity passenger)
    {
        super.addPassenger(passenger);
        if(this.isControlledByLocalInstance() && this.lerpSteps > 0)
        {
            this.lerpSteps = 0;
            this.setPos(this.lerpX, this.lerpY, this.lerpZ);
            this.yRot = (float) this.lerpYaw;
            this.xRot = (float) this.lerpPitch;
        }

        // Makes the player face the same direction of the vehicle
        passenger.yRot = this.getYRot();
        passenger.xRot = this.getXRot();

        // Resets the passenger yaw offset
        if(passenger instanceof Player && ((Player) passenger).isLocalPlayer())
        {
            this.passengerYawOffset = 0;
            this.passengerPitchOffset = 0;
        }
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger)
    {
        return this.getPassengers().size() < this.getProperties().getSeats().size();
    }

//    @Override
//    public void positionRider(@NotNull Entity passenger)
//    {
//        super.positionRider(passenger);
//        this.updatePassengerPosition(passenger);
//    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        VehicleProperties properties = this.getProperties();
        int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());

        if (seatIndex >= 0 && seatIndex < properties.getSeats().size()) {
            Seat seat = properties.getSeats().get(seatIndex);

            // Base offset from JSON
            Vec3 seatOffset = seat.getPosition()
                    .add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0)
                    .scale(properties.getBodyTransform().getScale())
                    .multiply(-1, 1, 1)
                    .scale(0.0625);

            // Rotate around vehicle yaw
            Vec3 rotated = seatOffset.yRot((float) Math.toRadians(-this.getYRot()));

            double x = this.getX() + rotated.x;
            double y = this.getY() + rotated.y + passenger.getMyRidingOffset();
            double z = this.getZ() + rotated.z;

            // Correct call for 1.20.1
            callback.accept(passenger, x, y, z);

            // Sync rider rotation with vehicle
            if (passenger instanceof LivingEntity living) {
                living.setYBodyRot(this.getYRot());
                living.setYHeadRot(this.getYRot());
            }
        } else {
            super.positionRider(passenger, callback);
        }
    }


    public boolean isAerialVehicle()
    {
        return this instanceof PlaneEntity || this instanceof HelicopterEntity;
    }


    protected void updatePassengerPosition(Entity passenger)
    {
        System.out.println(this.hasPassenger(passenger));
        if(this.hasPassenger(passenger))
        {
            int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
            System.out.println(seatIndex);
            if(seatIndex != -1)
            {
                VehicleProperties properties = this.getProperties();
                if(seatIndex >= 0 && seatIndex < properties.getSeats().size())
                {
                    Seat seat = properties.getSeats().get(seatIndex);
                    Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(properties.getBodyTransform().getTranslate()).scale(0.0625).yRot(-(this.getYRot() + 180) * 0.017453292F);
                    passenger.setPos(this.getX() - seatVec.x, this.getY() + seatVec.y + passenger.getMyRidingOffset(), this.getZ() - seatVec.z);
                    if(this.level().isClientSide())
                    {
                        boolean isAerial = this.isAerialVehicle();
                        if(isAerial)
                        {
                            //TODO launch the game to test this
                            if(Config.CLIENT.immersiveCamera.get() && Config.CLIENT.shouldFollowPitch.get())
                            {
                                passenger.xRotO = passenger.getXRot();
                                passenger.xRot = this.getXRot() + this.passengerPitchOffset;
                            }
                            if(this.canApplyYawOffset(passenger) && Config.CLIENT.shouldFollowYaw.get())
                            {

                                passenger.yRot -= Mth.degreesDifference(this.yRot - this.passengerYawOffset, passenger.getYRot());
                                passenger.setYHeadRot(passenger.getYRot());



                            }
                        }

                        this.clampYaw(passenger);
                    }
                }
            }
        }
    }

    protected void updatePassengerPositionManual(Entity passenger) {
        Vec3 offset = this.getProperties().getHeldOffset().yRot(passenger.getYRot() * ((float)Math.PI / 180F));
        passenger.setPos(this.getX() - offset.z * 0.0625, this.getY() - offset.y * 0.0625 - 2 * 0.0625, this.getZ() - offset.x * 0.0625);
    }

    protected double getMountedHeightOffset() {
        return 1.0D; // Custom height if needed
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (!this.getPassengers().isEmpty()) {
            this.updatePassengerPositionManual(this.getPassengers().get(0));
        }
    }

    public boolean canApplyYawOffset(Entity passenger)
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    protected void clampYaw(Entity passenger)
    {
        int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
        float seatYawOffset = seatIndex != -1 ? this.getProperties().getSeats().get(seatIndex).getYawOffset() : 0F;
        passenger.setYBodyRot(this.getYRot() + seatYawOffset);
        float wrappedYaw = Mth.wrapDegrees(passenger.getYRot() - this.getYRot() - seatYawOffset);
        float clampedYaw = Mth.clamp(wrappedYaw, -120.0F, 120.0F);
        passenger.yRotO += clampedYaw - wrappedYaw;
        passenger.yRot += clampedYaw - wrappedYaw;

        passenger.setYHeadRot(passenger.getYRot());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPassengerTurned(@NotNull Entity passenger)
    {
        this.clampYaw(passenger);
        if(VehicleHelper.canFollowVehicleOrientation(passenger))
        {
            this.updatePassengerOffsets(passenger);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updatePassengerOffsets(Entity passenger)
    {
        int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
        float seatYawOffset = seatIndex != -1 ? this.getProperties().getSeats().get(seatIndex).getYawOffset() : 0F;
        Vec3 vehicleForward = Vec3.directionFromRotation(0, this.yRot);
        Vec3 passengerForward = Vec3.directionFromRotation(passenger.getXRot(), passenger.getYHeadRot());
        this.passengerPitchOffset = Mth.degreesDifference(CommonUtils.pitch(passengerForward), CommonUtils.pitch(vehicleForward)) - this.xRot;

        if(!this.canApplyYawOffset(passenger))
        {
            this.passengerYawOffset = 0;
        }
        else
        {
            this.passengerYawOffset = Mth.degreesDifference(Mth.wrapDegrees(passenger.getYRot()) + seatYawOffset, CommonUtils.yaw(vehicleForward));
            this.passengerYawOffset += seatYawOffset;
        }
    }

    protected void updateBodyRotations()
    {
        this.bodyRotationYaw = this.getYRot();
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationPitch(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.prevBodyRotationPitch, this.bodyRotationPitch);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationYaw(float partialTicks)
    {
        return Mth.rotLerp(partialTicks, this.prevBodyRotationYaw, this.bodyRotationYaw);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationRoll(float partialTicks)
    {
        return Mth.rotLerp(partialTicks, this.prevBodyRotationRoll, this.bodyRotationRoll);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewPitch(float partialTicks)
    {
        return this.getBodyRotationPitch(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewYaw(float partialTicks)
    {
        return this.getBodyRotationYaw(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewRoll(float partialTicks)
    {
        return this.getBodyRotationRoll(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getPassengerYawOffset()
    {
        return this.passengerYawOffset;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPassengerPitchOffset()
    {
        return this.passengerPitchOffset;
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateWheelRotations() {}

    @OnlyIn(Dist.CLIENT)
    public float getWheelRotation(@Nullable Wheel wheel, float partialTicks)
    {
        return 0F;
    }
}