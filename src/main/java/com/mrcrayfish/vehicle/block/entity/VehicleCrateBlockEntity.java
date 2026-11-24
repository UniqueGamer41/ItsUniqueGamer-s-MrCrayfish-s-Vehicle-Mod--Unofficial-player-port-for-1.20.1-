package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.entity.EngineTier;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateBlockEntity extends BlockEntitySynced
{
    private ResourceLocation entityId;
    private int color = VehicleEntity.DYE_TO_COLOR[0];
    private ItemStack engineStack = ItemStack.EMPTY;
    private ItemStack wheelStack = ItemStack.EMPTY;
    private boolean opened = false;
    private int timer;
    private UUID opener;

    @OnlyIn(Dist.CLIENT)
    private Entity entity;

    public VehicleCrateBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.VEHICLE_CRATE.get(), pos, state);
    }

    public static void onServerTick(Level level, BlockPos pos, BlockState state, VehicleCrateBlockEntity entity)
    {
        entity.onServerTick();
    }

    public static void onClientTick(Level level, BlockPos pos, BlockState state, VehicleCrateBlockEntity entity)
    {
        entity.onClientTick();
    }

    public void setEntityId(ResourceLocation entityId)
    {
        this.entityId = entityId;
        this.setChanged();
    }

    public ResourceLocation getEntityId()
    {
        return entityId;
    }

    public void open(UUID opener)
    {
        if(this.entityId != null)
        {
            this.opened = true;
            this.opener = opener;
            this.syncToClient();
        }
    }

    public boolean isOpened()
    {
        return opened;
    }

    public int getTimer()
    {
        return timer;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public <E extends Entity> E getEntity()
    {
        return (E) entity;
    }

    protected void onServerTick()
    {
        if(this.opened)
        {
            this.timer += 5;

            if(this.timer > 250)
            {
                BlockState state = this.level.getBlockState(this.worldPosition);
                Direction facing = state.getValue(VehicleCrateBlock.DIRECTION);
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(this.entityId);
                if(entityType != null)
                {
                    Entity entity = entityType.create(this.level);
                    if(entity != null)
                    {
                        if(entity instanceof VehicleEntity vehicleEntity)
                        {
                            vehicleEntity.setColor(this.color);
                            if(!this.wheelStack.isEmpty())
                            {
                                vehicleEntity.setWheelStack(this.wheelStack);
                            }
                        }
                        if(this.opener != null && entity instanceof PoweredVehicleEntity poweredVehicle)
                        {
                            poweredVehicle.setOwner(this.opener);
                            if(!this.engineStack.isEmpty())
                            {
                                poweredVehicle.setEngineStack(this.engineStack);
                            }
                        }
                        entity.absMoveTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5, facing.get2DDataValue() * 90F + 180F, 0F);
                        entity.setYHeadRot(facing.get2DDataValue() * 90F + 180F);
                        this.level.addFreshEntity(entity);
                    }
                    this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    protected void onClientTick()
    {
        if(this.opened)
        {
            this.onServerTick();

            if(this.entityId != null && this.entity == null)
            {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(this.entityId);
                if(entityType != null)
                {
                    this.entity = entityType.create(this.level);
                    if(this.entity != null)
                    {
                        VehicleHelper.playSound(SoundEvents.ITEM_BREAK, this.worldPosition, 1.0F, 0.5F);
                        List<SynchedEntityData.DataValue<?>> entryList = this.entity.getEntityData().getNonDefaultValues();
                        if(entryList != null)
                        {
                            entryList.forEach(dataEntry -> this.entity.onSyncedDataUpdated(dataEntry.serializer().createAccessor(dataEntry.id())));
                        }
                        if(this.entity instanceof VehicleEntity vehicleEntity)
                        {
                            vehicleEntity.setColor(this.color);
                            if(!this.wheelStack.isEmpty())
                            {
                                vehicleEntity.setWheelStack(this.wheelStack);
                            }
                        }
                        if(this.entity instanceof PoweredVehicleEntity entityPoweredVehicle)
                        {
                            if(this.engineStack != null)
                            {
                                entityPoweredVehicle.setEngineStack(this.engineStack);
                            }
                        }
                    }
                    else
                    {
                        this.entityId = null;
                    }
                }
                else
                {
                    this.entityId = null;
                }
            }
            if(this.timer == 90 || this.timer == 110 || this.timer == 130 || this.timer == 150)
            {
                float pitch = (float) (0.9F + 0.2F * this.level.random.nextDouble());
                VehicleHelper.playSound(ModSounds.BLOCK_VEHICLE_CRATE_PANEL_LAND.get(), this.worldPosition, 1.0F, pitch);
            }
            if(this.timer == 150)
            {
                VehicleHelper.playSound(SoundEvents.GENERIC_EXPLODE, this.worldPosition, 1.0F, 1.0F);
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, false, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);

        if(compound.contains("vehicle", Tag.TAG_STRING))
        {
            this.entityId = new ResourceLocation(compound.getString("vehicle"));
        }

        this.color = compound.getInt(IDyeable.NBT_KEY);

        if(compound.contains("engineStack", Tag.TAG_COMPOUND))
        {
            this.engineStack = ItemStack.of(compound.getCompound("engineStack"));
        }
        else if(compound.getBoolean("creative"))
        {
            VehicleProperties properties = VehicleProperties.get(this.entityId);
            EngineItem engineItem = VehicleRegistry.getEngineItem(properties.getExtended(PoweredProperties.class).getEngineType(), EngineTier.IRON);
            this.engineStack = engineItem != null ? new ItemStack(engineItem) : ItemStack.EMPTY;
        }
        if(compound.contains("wheelStack", Tag.TAG_COMPOUND))
        {
            this.wheelStack = ItemStack.of(compound.getCompound("wheelStack"));
        }
        else
        {
            this.wheelStack = new ItemStack(ModItems.STANDARD_WHEEL.get());
        }
        if(compound.contains("opener", Tag.TAG_STRING))
        {
            this.opener = compound.getUUID("opener");
        }

        this.opened = compound.getBoolean("opened");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        if(this.entityId != null)
        {
            compound.putString("vehicle", this.entityId.toString());
        }

        if(this.opener != null)
        {
            compound.putUUID("opener", this.opener);
        }

        if(!this.engineStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "engineStack", this.engineStack);
        }

        if(!this.wheelStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "wheelStack", this.wheelStack);
        }

        compound.putInt(IDyeable.NBT_KEY, this.color);
        compound.putBoolean("opened", this.opened);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }
}
