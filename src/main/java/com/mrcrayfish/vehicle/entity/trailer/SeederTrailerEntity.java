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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class SeederTrailerEntity extends TrailerEntity implements IStorage
{
    private static final String INVENTORY_STORAGE_KEY = "Inventory";

    private int inventoryTimer;
    private StorageInventory inventory;

    public SeederTrailerEntity(EntityType<? extends SeederTrailerEntity> type, Level worldIn)
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
        if(!this.level().isClientSide && Config.SERVER.trailerInventorySyncCooldown.get() > 0 && inventoryTimer++ == Config.SERVER.trailerInventorySyncCooldown.get())
        {
            this.inventoryTimer = 0;
          //  PacketHandler.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> SeederTrailerEntity.this), new MessageSyncStorage(this, INVENTORY_STORAGE_KEY));
        }
    }

    @Override
    public void onUpdateVehicle()
    {
        super.onUpdateVehicle();

        Vec3 lookVec = this.getLookAngle();
        this.plantSeed(lookVec.yRot((float) Math.toRadians(90F)).scale(0.85));
        this.plantSeed(Vec3.ZERO);
        this.plantSeed(lookVec.yRot((float) Math.toRadians(-90F)).scale(0.85));
    }

    private void plantSeed(Vec3 vec)

    {

        BlockPos pos = BlockPos.containing(this.getX() + vec.x, this.getY() + 0.25, this.getZ() + vec.z);

        if(level().isEmptyBlock(pos) && level().getBlockState(pos.below()).getBlock() instanceof FarmBlock)

        {

            ItemStack seed = this.getSeed();

            if(seed.isEmpty() && this.getTrailer() instanceof StorageTrailerEntity)

            {

                seed = this.getSeedFromStorage((StorageTrailerEntity) this.getPullingEntity());

            }

            if(this.isSeed(seed) || seed.getItem() instanceof IPlantable || seed.is(Tags.Items.CROPS))

            {

                Block seedBlock = ((BlockItem) seed.getItem()).getBlock();

                this.level().setBlockAndUpdate(pos, seedBlock.defaultBlockState());

                seed.shrink(1);

            }

        }

    }

    private ItemStack getSeed()
    {
        for(int i = 0; i < this.inventory.getContainerSize(); i++)
        {
            ItemStack stack = this.inventory.getItem(i);
            if(this.isSeed(stack))
            {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isSeed(ItemStack stack)
    {
        return !stack.isEmpty() && ((ItemNameBlockItem) stack.getItem()).getBlock() instanceof CropBlock;
    }

    private ItemStack getSeedFromStorage(StorageTrailerEntity storageTrailer)
    {
        if(storageTrailer == null)
            return ItemStack.EMPTY;

        if(storageTrailer.getInventory() != null)
        {
            StorageInventory storage = storageTrailer.getInventory();
            for(int i = 0; i < storage.getContainerSize(); i++)
            {
                ItemStack stack = storage.getItem(i);
                if(this.isSeed(stack))
                {
                    return stack;
                }
            }

            if(storageTrailer.getTrailer() instanceof StorageTrailerEntity)
            {
                return this.getSeedFromStorage((StorageTrailerEntity) storageTrailer.getPullingEntity());
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
                !stack.isEmpty() && stack.is(Tags.Items.SEEDS) || stack.is(Tags.Items.CROPS) || stack.getItem() instanceof IPlantable);
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
        EntityRayTracer.instance().registerInteractionBox(ModEntities.SEEDER.get(), () -> {
            return createScaledBoundingBox(-7.0, 1.5, 6.0, 7.0, 3.5, 17.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
