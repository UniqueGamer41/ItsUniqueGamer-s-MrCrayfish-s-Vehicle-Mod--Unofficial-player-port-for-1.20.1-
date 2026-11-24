package com.mrcrayfish.vehicle.entity.vehicle;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.inventory.IAttachableChest;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.MotorcycleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachChest;
import com.mrcrayfish.vehicle.network.message.MessageOpenStorage;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class MopedEntity extends MotorcycleEntity implements IStorage, IAttachableChest
{
    private static final EntityDataAccessor<Boolean> CHEST = SynchedEntityData.defineId(MopedEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHEST_OPEN = SynchedEntityData.defineId(MopedEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private StorageInventory inventory;

    @OnlyIn(Dist.CLIENT)
    private float openProgress;
    @OnlyIn(Dist.CLIENT)
    private float prevOpenProgress;

    public MopedEntity(EntityType<? extends MopedEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.initInventory();
        this.setMaxSpeed(8.0F);
        this.setMaxTurnAngle(45);
        this.setTurnSensitivity(15);
    }

    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(CHEST, false);
        this.entityData.define(CHEST_OPEN, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if(compound.getBoolean("ChestAttached"))
        {
            this.setChest(true);
            this.initInventory();
            this.readInventories(compound);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        if(this.hasChest())
        {
            compound.putBoolean("ChestAttached", true);
            this.writeInventories(compound);
        }
    }

    public boolean hasChest()
    {
        return this.hasChest("");
    }

    @Override
    public boolean hasChest(String key)
    {
        return this.entityData.get(CHEST);
    }

    public void setChest(boolean chest)
    {
        this.entityData.set(CHEST, chest);
    }

    private void initInventory()
    {
        StorageInventory original = this.inventory;
        this.inventory = new ChestInventory(this, this.getDisplayName(), 3);
        // Copies the inventory if it exists already over to the new instance
        if(original != null)
        {
            for(int i = 0; i < original.getContainerSize(); i++)
            {
                ItemStack stack = original.getItem(i);
                if(!stack.isEmpty())
                {
                    this.inventory.setItem(i, stack.copy());
                }
            }
        }
    }

    @Override
    public Map<String, StorageInventory> getStorageInventories()
    {
        if(this.hasChest() && this.inventory != null)
        {
            return ImmutableMap.of("Chest", this.inventory);
        }
        return ImmutableMap.of();
    }

    @Override
    protected void onVehicleDestroyed(LivingEntity entity)
    {
        super.onVehicleDestroyed(entity);
        if(this.hasChest() && this.inventory != null)
        {
            Containers.dropContents(this.level(), this, this.inventory);
        }
    }

    @Override
    public void attachChest(String key, ItemStack stack)
    {
        if(!stack.isEmpty() && stack.getItem() == Items.CHEST)
        {
            this.setChest(true);
            this.initInventory();
            CompoundTag itemTag = stack.getTag();
            if(itemTag != null)
            {
                CompoundTag blockEntityTag = itemTag.getCompound("BlockEntityTag");
                if(!blockEntityTag.isEmpty() && blockEntityTag.contains("Items", Tag.TAG_LIST))
                {
                    NonNullList<ItemStack> chestInventory = NonNullList.withSize(27, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(blockEntityTag, chestInventory);
                    for(int i = 0; i < chestInventory.size(); i++)
                    {
                        this.inventory.setItem(i, chestInventory.get(i));
                    }
                }
            }
        }
    }

    @Override
    public void removeChest(String key)
    {
        if(this.hasChest() && this.inventory != null)
        {
            Vec3 target = this.getChestPosition();
            InventoryUtil.dropInventoryItems(this.level(), target.x, target.y, target.z, this.inventory);
            this.setChest(false);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level().addFreshEntity(new ItemEntity(level(), target.x, target.y, target.z, new ItemStack(Blocks.CHEST)));
            this.inventory = null;
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if(this.hasChest())
        {
            // Updates the chest open state
            if(!this.level().isClientSide())
            {
                this.entityData.set(CHEST_OPEN, this.getPlayerCountInChest() > 0);
            }
            else
            {
                // Updates the open progress for the animation
                this.prevOpenProgress = this.openProgress;
                if(this.entityData.get(CHEST_OPEN))
                {
                    this.openProgress = Math.min(1.0F, this.openProgress + 0.1F);
                }
                else
                {
                    float lastOpenProgress = this.openProgress;
                    this.openProgress = Math.max(0.0F, this.openProgress - 0.1F);
                    if(this.openProgress < 0.5F && lastOpenProgress >= 0.5F)
                    {
                        Vec3 target = this.getChestPosition();
                        this.level().playLocalSound(target.x, target.y, target.z, SoundEvents.CHEST_CLOSE, this.getSoundSource(), 0.5F, this.level().random.nextFloat() * 0.1F + 0.9F, false);
                    }
                }
            }
        }
    }

    protected Vec3 getChestPosition()
    {
        return new Vec3(0, 1.0, -0.75).yRot(-(this.getYRot()) * 0.017453292F).add(this.position());
    }

    protected int getPlayerCountInChest()
    {
        if(!this.hasChest())
        {
            return 0;
        }

        int count = 0;
        for(Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0F)))
        {
            if(player.containerMenu instanceof StorageContainer)
            {
                Container container = ((StorageContainer) player.containerMenu).getStorageInventory();
                if(container == this.inventory)
                {
                    count++;
                }
            }
        }
        return count;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRayTracer.instance().registerInteractionBox(ModEntities.MOPED.get(), () -> {
            return createScaledBoundingBox(-3.5, 8.0, -7.0, 3.5, 15.0, -14.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageOpenStorage(entity.getId(), "Chest"));
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
        }, MopedEntity::hasChest);

        EntityRayTracer.instance().registerInteractionBox(ModEntities.MOPED.get(), () -> {
            return createScaledBoundingBox(-4.0, 7.0, -6.5, 4.0, 8.0, -14.5, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachChest(entity.getId(), "Chest"));
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> !entity.hasChest());
    }

    @OnlyIn(Dist.CLIENT)
    public float getOpenProgress()
    {
        return this.openProgress;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPrevOpenProgress()
    {
        return this.prevOpenProgress;
    }

    public class ChestInventory extends StorageInventory
    {
        public ChestInventory(Entity entity, Component displayName, int rows)
        {
            super(entity, displayName, rows);
        }

        @Override
        public void startOpen(Player player)
        {
            Vec3 target = MopedEntity.this.getChestPosition();
            player.level().playSound(null, target.x, target.y, target.z, SoundEvents.CHEST_OPEN, MopedEntity.this.getSoundSource(), 0.5F, 0.9F);
        }
    }
}
