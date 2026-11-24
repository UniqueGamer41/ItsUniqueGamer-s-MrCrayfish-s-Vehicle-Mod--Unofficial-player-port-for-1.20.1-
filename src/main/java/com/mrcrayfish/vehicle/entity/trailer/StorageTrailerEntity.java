package com.mrcrayfish.vehicle.entity.trailer;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import com.mrcrayfish.vehicle.network.message.MessageOpenStorage;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class StorageTrailerEntity extends TrailerEntity implements IStorage
{
    private static final String INVENTORY_STORAGE_KEY = "Inventory";

    private StorageInventory inventory;

    public StorageTrailerEntity(EntityType<? extends StorageTrailerEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.initInventory();
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger)
    {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if(compound.contains(INVENTORY_STORAGE_KEY, Tag.TAG_LIST))
        {
            this.initInventory();
            InventoryUtil.readInventoryToNBT(compound, INVENTORY_STORAGE_KEY, this.inventory);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        if(this.inventory != null)
        {
            InventoryUtil.writeInventoryToNBT(compound, INVENTORY_STORAGE_KEY, this.inventory);
        }
    }

    private void initInventory()
    {
        StorageInventory original = this.inventory;
        this.inventory = new StorageInventory(this, this.getDisplayName(), 3);
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
    protected void onVehicleDestroyed(LivingEntity entity)
    {
        super.onVehicleDestroyed(entity);
        if(this.inventory != null)
        {
            Containers.dropContents(this.level(), this, this.inventory);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRayTracer.instance().registerInteractionBox(ModEntities.STORAGE_TRAILER.get(), () -> {
            return createScaledBoundingBox(-6.0, -0.5, 9.0, 6.0, 3.5, 17.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);

        EntityRayTracer.instance().registerInteractionBox(ModEntities.STORAGE_TRAILER.get(), () -> {
            double chestScale = 0.9;
            double bodyScale = 1.0 / chestScale;
            return createScaledBoundingBox(-0.4375, 0.125 * bodyScale, -0.4375, 0.4375, 0.9125 * bodyScale, 0.4375, chestScale);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageOpenStorage(entity.getId(), INVENTORY_STORAGE_KEY));
                minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }

    @Override
    public Map<String, StorageInventory> getStorageInventories()
    {
        return ImmutableMap.of(INVENTORY_STORAGE_KEY, this.inventory);
    }

    public StorageInventory getInventory()
    {
        return this.inventory;
    }
}
