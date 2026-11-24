package com.mrcrayfish.vehicle.entity.trailer;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import com.mrcrayfish.vehicle.network.message.MessageSyncStorage;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class FertilizerTrailerEntity extends TrailerEntity implements IStorage
{
    private static final String INVENTORY_STORAGE_KEY = "Inventory";

    private int inventoryTimer;
    private StorageInventory inventory;
    private BlockPos[] lastPos = new BlockPos[3];

    public FertilizerTrailerEntity(EntityType<? extends FertilizerTrailerEntity> type, Level worldIn)
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
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack heldItem = player.getItemInHand(hand);
        if((heldItem.isEmpty() || !(heldItem.getItem() instanceof SprayCanItem)) && player instanceof ServerPlayer)
        {
            IStorage.openStorage((ServerPlayer) player, this, INVENTORY_STORAGE_KEY);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void tick()
    {
        super.tick();

        if(!this.level().isClientSide() && Config.SERVER.trailerInventorySyncCooldown.get() > 0 && this.inventoryTimer++ == Config.SERVER.trailerInventorySyncCooldown.get())
        {
            this.inventoryTimer = 0;
            //PacketHandler.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new MessageSyncStorage(this, INVENTORY_STORAGE_KEY));
        }
    }

    @Override
    public void onUpdateVehicle()
    {
        super.onUpdateVehicle();

        if(!level().isClientSide)
        {
            ItemStack fertilizer = this.getFertilizer();
            if(fertilizer.isEmpty() && this.getPullingEntity() instanceof StorageTrailerEntity)
            {
                fertilizer = this.getFertilizerFromStorage((StorageTrailerEntity) this.getPullingEntity());
            }
            if(!fertilizer.isEmpty())
            {
                Vec3 lookVec = this.getLookAngle();
                boolean applied = this.applyFertilizer(lookVec.yRot((float) Math.toRadians(90F)), 0);
                applied |= this.applyFertilizer(Vec3.ZERO, 1);
                applied |= this.applyFertilizer(lookVec.yRot((float) Math.toRadians(-90F)), 2);
                if(applied) fertilizer.shrink(1);
            }
        }
    }

    private boolean applyFertilizer(Vec3 vec, int index)
    {
        Vec3 prevPosVec = new Vec3(xo, yo + 0.25, zo);
        prevPosVec = prevPosVec.add(new Vec3(0, 0, -1).yRot(-this.getYRot() * 0.017453292F));
        BlockPos pos = new BlockPos((int) (prevPosVec.x + vec.x), (int) prevPosVec.y, (int) (prevPosVec.z + vec.z));

        if(lastPos[index] != null && lastPos[index].equals(pos))
        {
            return false;
        }
        lastPos[index] = pos;

        BlockState state = level().getBlockState(pos);
        if(state.getBlock() instanceof BonemealableBlock growable)
        {
            if(growable.isValidBonemealTarget(level(), pos, state, false))
            {
                if(growable.isBonemealSuccess(level(), random, pos, state))
                {
                    growable.performBonemeal((ServerLevel) level(), random, pos, state);
                    level().levelEvent(2005, pos, 0);
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack getFertilizer()
    {
        for(int i = 0; i < this.inventory.getContainerSize(); i++)
        {
            ItemStack stack = this.inventory.getItem(i);
            if(!stack.isEmpty() && stack.getItem() instanceof BoneMealItem)
            {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getFertilizerFromStorage(StorageTrailerEntity storageTrailer)
    {
        if(storageTrailer == null)
            return ItemStack.EMPTY;

        if(storageTrailer.getStorageInventories() != null)
        {
            StorageInventory storage = storageTrailer.getInventory();
            for(int i = 0; i < storage.getContainerSize(); i++)
            {
                ItemStack stack = storage.getItem(i);
                if(!stack.isEmpty() && stack.getItem() instanceof BoneMealItem)
                {
                    return stack;
                }
            }

            if(storageTrailer.getPullingEntity() instanceof StorageTrailerEntity)
            {
                return this.getFertilizerFromStorage((StorageTrailerEntity) storageTrailer.getPullingEntity());
            }
        }
        return ItemStack.EMPTY;
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
        this.inventory = new StorageInventory(this, this.getDisplayName(), 3, stack ->
                !stack.isEmpty() && stack.getItem() instanceof BoneMealItem);
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

    @Override
    public Map<String, StorageInventory> getStorageInventories()
    {
        return ImmutableMap.of(INVENTORY_STORAGE_KEY, this.inventory);
    }

    public StorageInventory getInventory()
    {
        return this.inventory;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        EntityRayTracer.instance().registerInteractionBox(ModEntities.FERTILIZER.get(), () -> {
            return createScaledBoundingBox(-7.0, 1.5, 7.0, 7.0, 3.5, 18.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
