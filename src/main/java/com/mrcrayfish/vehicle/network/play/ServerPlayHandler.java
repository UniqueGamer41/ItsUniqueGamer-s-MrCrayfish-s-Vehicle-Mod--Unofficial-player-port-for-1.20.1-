package com.mrcrayfish.vehicle.network.play;

import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.common.CommonEvents;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.SeatTracker;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.common.inventory.IAttachableChest;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipe;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipes;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.IEngineType;
import com.mrcrayfish.vehicle.entity.PlaneEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.WheelItem;
import com.mrcrayfish.vehicle.network.message.*;
import com.mrcrayfish.vehicle.block.entity.WorkstationBlockEntity;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler
{
    public static void handleAttachChestMessage(MessageAttachChest message, ServerPlayer player)
    {
        Level world = player.level();
        Entity targetEntity = world.getEntity(message.getEntityId());
        if(targetEntity instanceof IAttachableChest)
        {
            float reachDistance = (float) player.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue(); //TODO TEST
            if(player.distanceTo(targetEntity) < reachDistance)
            {
                IAttachableChest attachableChest = (IAttachableChest) targetEntity;
                if(!attachableChest.hasChest(message.getKey()))
                {
                    ItemStack stack = player.getInventory().getSelected();
                    if(!stack.isEmpty() && stack.getItem() == Items.CHEST)
                    {
                        attachableChest.attachChest(message.getKey(), stack);
                        world.playSound(null, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    public static void handleAttachTrailerMessage(ServerPlayer player, MessageAttachTrailer message)
    {
        System.out.println("[DEBUG] Actually entered handleAttachTrailerMessage()");
        Entity trailerEntity = player.level().getEntity(message.getTrailerId());
        if(trailerEntity instanceof TrailerEntity trailer)
        {

            if(player.getVehicle() != null) {
                System.out.println("[DEBUG] VEHICLE IS NOT NULL");
            }
            else if(player.getVehicle() == null)
            {
                System.out.println("[DEBUG] VEHICLE IS NULL, PROCEEDING AHEAD...");
                trailer.setPullingEntity(player);
                SyncedEntityData.instance().set(player, ModDataKeys.TRAILER, message.getTrailerId());
            }
        }
    }

    public static void handleCraftVehicleMessage(ServerPlayer player, MessageCraftVehicle message)
    {
        System.out.println("[DEBUG] Actually entered handleCraftVehicleMessage()");
        if (player == null) {
            System.out.println("[DEBUG] Player is NULL, skipping handle.");
            return;
        }
        Level world = player.level();


        if(!(player.containerMenu instanceof WorkstationContainer workstation)){
            System.out.println("[DEBUG] Player is NOT using a WorkstationContainer");
            return;
        }


        ResourceLocation entityId = new ResourceLocation(message.getVehicleId());
        if(Config.SERVER.disabledVehicles.get().contains(entityId.toString()))
            return;

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        System.out.println("[DEBUG] Retrieved entity type: " + entityType);
        if(entityType == null)
            return;

        System.out.println("[DEBUG] after entity");

//        if(!VehicleRegistry.getRegisteredVehicles().contains(ForgeRegistries.ENTITY_TYPES.getKey(entityType)))
//            return;

        System.out.println("[DEBUG/DOUBT] After vehicle registery");

        WorkstationRecipe recipe = WorkstationRecipes.getRecipe(entityType, world);
        if(recipe == null || !recipe.hasMaterials(player))
            return;

        Entity entity = entityType.create(world);
        if(!(entity instanceof VehicleEntity vehicle))
            return;

        System.out.println("Handling vehicle craft for: " + message.getVehicleId());
        System.out.println("Disabled vehicles: " + Config.SERVER.disabledVehicles.get());

        System.out.println("EntityType: " + entityType);
        System.out.println("Registered: " + VehicleRegistry.getRegisteredVehicles().contains(entityId));
        System.out.println("Recipe: " + recipe);
        System.out.println("Has Materials: " + (recipe != null && recipe.hasMaterials(player)));

        IEngineType engineType = EngineType.NONE;
        if(vehicle instanceof PoweredVehicleEntity)
        {
            PoweredVehicleEntity entityPoweredVehicle = (PoweredVehicleEntity) entity;
            engineType = entityPoweredVehicle.getEngineType();

            WorkstationBlockEntity workstationTileEntity = workstation.getTileEntity();
            ItemStack workstationEngine = workstationTileEntity.getItem(1);
//            System.out.println("[DEBUG] Created crate stack: " + stack);
            if(workstationEngine.isEmpty() || !(workstationEngine.getItem() instanceof EngineItem))
                return;

            IEngineType engineType2 = ((EngineItem) workstationEngine.getItem()).getEngineType();
            if(engineType != EngineType.NONE && engineType != engineType2)
                return;

            if(entityPoweredVehicle.canChangeWheels())
            {
                ItemStack wheel = workstationTileEntity.getInventory().get(2);
                if(!(wheel.getItem() instanceof WheelItem))
                    return;
            }
        }

        /* At this point we have verified the crafting and can perform irreversible actions */

        recipe.consumeMaterials(player);

        WorkstationBlockEntity workstationTileEntity = workstation.getTileEntity();
        BlockPos pos = workstationTileEntity.getBlockPos();

        /* Gets the color based on the dye */
        int color = VehicleEntity.DYE_TO_COLOR[0];
        if(vehicle.getProperties().canBePainted())
        {
            ItemStack workstationDyeStack = workstationTileEntity.getInventory().get(0);
            if(workstationDyeStack.getItem() instanceof DyeItem dyeItem)
            {
                color = dyeItem.getDyeColor().getTextColor();
                workstationTileEntity.getInventory().set(0, ItemStack.EMPTY);
            }
        }

        ItemStack engineStack = ItemStack.EMPTY;
        if(engineType != EngineType.NONE)
        {
            ItemStack workstationEngineStack = workstationTileEntity.getInventory().get(1);
            if(workstationEngineStack.getItem() instanceof EngineItem)
            {
                engineStack = workstationEngineStack.copy();
                workstationTileEntity.getInventory().set(1, ItemStack.EMPTY);
            }
        }

        ItemStack wheelStack = ItemStack.EMPTY;
        if(vehicle instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) vehicle).canChangeWheels())
        {
            ItemStack workstationWheelStack = workstationTileEntity.getInventory().get(2);
            if(workstationWheelStack.getItem() instanceof WheelItem)
            {
                wheelStack = workstationWheelStack.copy();
                workstationTileEntity.getInventory().set(2, ItemStack.EMPTY);
            }
        }

        ItemStack stack = VehicleCrateBlock.create(entityId, color, engineStack, wheelStack);
        if (!player.getInventory().add(stack)) {
            // Drop it at workstation if inventory full
            world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack));
        }
        //world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack));
        world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.2F);

    }

    public static void handleCycleSeatsMessage(ServerPlayer player, MessageCycleSeats message)
    {
        Entity entity = player.getVehicle();
        if(!(entity instanceof VehicleEntity))
            return;

        VehicleEntity vehicle = (VehicleEntity) player.getVehicle();
        List<Seat> seats = vehicle.getProperties().getSeats();

        /* No need to cycle if already full of passengers */
        if(vehicle.getPassengers().size() >= seats.size())
            return;

        SeatTracker tracker = vehicle.getSeatTracker();
        int seatIndex = tracker.getSeatIndex(player.getUUID());
        for(int i = 0; i < seats.size() - 1; i++)
        {
            int nextIndex = (seatIndex + (i + 1)) % seats.size();
            if(tracker.isSeatAvailable(nextIndex))
            {
                tracker.setSeatIndex(nextIndex, player.getUUID());
                vehicle.onPlayerChangeSeat(player, seatIndex, nextIndex);
                return;
            }
        }
    }

    public static void handleSetSeatMessage(ServerPlayer player, MessageSetSeat message)
    {
        Entity entity = player.getVehicle();
        if(!(entity instanceof VehicleEntity))
            return;

        VehicleEntity vehicle = (VehicleEntity) player.getVehicle();
        SeatTracker tracker = vehicle.getSeatTracker();
        if(!tracker.isSeatAvailable(message.getIndex()))
            return;

        int seatIndex = tracker.getSeatIndex(player.getUUID());
        tracker.setSeatIndex(message.getIndex(), player.getUUID());
        vehicle.onPlayerChangeSeat(player, seatIndex, message.getIndex());
    }

    public static void handleFuelVehicleMessage(ServerPlayer player, MessageFuelVehicle message)
    {
        Entity targetEntity = player.level().getEntity(message.getEntityId());
        if(targetEntity instanceof PoweredVehicleEntity)
        {
            ((PoweredVehicleEntity) targetEntity).fuelVehicle(player, message.getHand());
        }
    }

    public static void handleHandbrakeMessage(ServerPlayer player, MessageHandbrake message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof PoweredVehicleEntity)
        {
            ((PoweredVehicleEntity) riding).setHandbraking(message.isHandbrake());
        }
    }

    public static void handleHelicopterInputMessage(ServerPlayer player, MessageHelicopterInput message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof HelicopterEntity helicopter)
        {
            helicopter.setLift(message.getLift());
            helicopter.setForwardInput(message.getForward());
            helicopter.setSideInput(message.getSide());
        }
    }

    public static void handleHitchTrailerMessage(ServerPlayer player, MessageHitchTrailer message)
    {
        if(!(player.getVehicle() instanceof VehicleEntity vehicle))
            return;

        if(!vehicle.canTowTrailers())
            return;

        if(!message.isHitch())
        {
            if(vehicle.getTrailer() != null)
            {
                vehicle.setTrailer(null);
                player.level().playSound(null, vehicle.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        else
        {
            VehicleProperties properties = vehicle.getProperties();
            Vec3 vehicleVec = vehicle.position();
            Vec3 towBarVec = properties.getTowBarOffset();
            towBarVec = new Vec3(towBarVec.x * 0.0625, towBarVec.y * 0.0625, towBarVec.z * 0.0625 + properties.getBodyTransform().getZ());
            vehicleVec = vehicleVec.add(towBarVec.yRot((float) Math.toRadians(-vehicle.getYRot())));

            AABB towBarBox = new AABB(vehicleVec.x, vehicleVec.y, vehicleVec.z, vehicleVec.x, vehicleVec.y, vehicleVec.z).inflate(0.25);
            List<TrailerEntity> trailers = player.level().getEntitiesOfClass(TrailerEntity.class, vehicle.getBoundingBox().inflate(5), input -> input.getPullingEntity() == null);
            for(TrailerEntity trailer : trailers)
            {
                if(trailer.getPullingEntity() != null)
                    continue;

                Vec3 trailerVec = trailer.position();
                Vec3 hitchVec = new Vec3(0, 0, -trailer.getHitchOffset() / 16.0);
                trailerVec = trailerVec.add(hitchVec.yRot((float) Math.toRadians(-trailer.getYRot())));
                AABB hitchBox = new AABB(trailerVec.x, trailerVec.y, trailerVec.z, trailerVec.x, trailerVec.y, trailerVec.z).inflate(0.25);
                if(towBarBox.intersects(hitchBox))
                {
                    vehicle.setTrailer(trailer);
                    player.level().playSound(null, vehicle.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 1.5F);
                    return;
                }
            }
        }
    }

    public static void handleHornMessage(ServerPlayer player, MessageHorn message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) riding).hasHorn())
        {
            ((PoweredVehicleEntity) riding).setHorn(message.isHorn());
        }
    }

    public static void handleInteractKeyMessage(ServerPlayer player, MessageInteractKey message)
    {
        Entity targetEntity = player.level().getEntity(message.getEntityId());
        if(targetEntity instanceof PoweredVehicleEntity poweredVehicle)
        {
            if(poweredVehicle.isKeyNeeded())
            {
                ItemStack stack = player.getMainHandItem();
                if(!stack.isEmpty() && stack.getItem() == ModItems.WRENCH.get())
                {
                    if(poweredVehicle.isOwner(player))
                    {
                        poweredVehicle.ejectKey();
                        poweredVehicle.setKeyNeeded(false);
                        CommonUtils.sendInfoMessage(player, "vehicle.status.key_removed");
                    }
                    else
                    {
                        CommonUtils.sendInfoMessage(player, "vehicle.status.invalid_owner");
                    }
                    return;
                }
                if(poweredVehicle.getKeyStack().isEmpty())
                {
                    if(!stack.isEmpty() && stack.getItem() == ModItems.KEY.get())
                    {
                        UUID keyUuid = stack.getOrCreateTag().getUUID("vehicleId");
                        if(poweredVehicle.getUUID().equals(keyUuid))
                        {
                            poweredVehicle.setKeyStack(stack.copy());
                            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        }
                        else
                        {
                            CommonUtils.sendInfoMessage(player, "vehicle.status.key_invalid");
                        }
                    }
                }
                else
                {
                    poweredVehicle.ejectKey();
                }
            }
        }
    }

    public static void handlePickupVehicleMessage(ServerPlayer player, MessagePickupVehicle message)
    {
        if(player.isCrouching())
        {
            Entity targetEntity = player.level().getEntity(message.getEntityId());
            if(targetEntity != null)
            {
                CommonEvents.handleVehicleInteraction(player.level(), player, InteractionHand.MAIN_HAND, targetEntity);
            }
        }
    }

    public static void handlePlaneInputMessage(ServerPlayer player, MessagePlaneInput message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof PlaneEntity plane)
        {
            plane.setLift(message.getLift());
            plane.setForwardInput(message.getForward());
            plane.setSideInput(message.getSide());
        }
    }

    public static void handleThrottleMessage(ServerPlayer player, MessageThrottle message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof PoweredVehicleEntity)
        {
            ((PoweredVehicleEntity) riding).setThrottle(message.getPower());
        }
    }

    public static void handleThrowVehicle(ServerPlayer player, MessageThrowVehicle message)
    {
        if(!player.isCrouching())
            return;

        //Spawns the vehicle and plays the placing sound
        if(!HeldVehicleDataHandler.isHoldingVehicle(player))
            return;

        CompoundTag heldTag = HeldVehicleDataHandler.getHeldVehicle(player);
        Optional<EntityType<?>> optional = EntityType.byString(heldTag.getString("id"));
        if(optional.isEmpty())
            return;

        EntityType<?> entityType = optional.get();
        Entity entity = entityType.create(player.level());
        if(entity instanceof VehicleEntity)
        {
            entity.load(heldTag);

            //Updates the player capability
            HeldVehicleDataHandler.setHeldVehicle(player, new CompoundTag());

            //Sets the positions and spawns the entity
            float rotation = (player.getYHeadRot() + 90F) % 360.0F;
            Vec3 heldOffset = ((VehicleEntity) entity).getProperties().getHeldOffset().yRot((float) Math.toRadians(-player.getYHeadRot()));

            //Gets the clicked vec if it was a right click block event
            Vec3 lookVec = player.getLookAngle();
            double posX = player.getX();
            double posY = player.getY() + player.getEyeHeight();
            double posZ = player.getZ();
            entity.absMoveTo(posX + heldOffset.x * 0.0625D, posY + heldOffset.y * 0.0625D, posZ + heldOffset.z * 0.0625D, rotation, 0F);

            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    motion.x() + lookVec.x,
                    motion.y() + lookVec.y,
                    motion.z() + lookVec.z
            );
            entity.fallDistance = 0.0F;

            player.level().addFreshEntity(entity);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ENTITY_VEHICLE_PICK_UP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void handleTurnAngleMessage(ServerPlayer player, MessageTurnAngle message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof PoweredVehicleEntity)
        {
            ((PoweredVehicleEntity) riding).setSteeringAngle(message.getAngle());
        }
    }

    public static void handleInteractCosmeticMessage(ServerPlayer player, MessageInteractCosmetic message)
    {
        Entity targetEntity = player.level().getEntity(message.getEntityId());
        if(!(targetEntity instanceof VehicleEntity vehicle))
            return;

        Collection<Action> cosmeticActions = vehicle.getCosmeticTracker().getActions(message.getCosmeticId());
        if(cosmeticActions == null)
        {
            VehicleMod.LOGGER.warn("Attempting to interact with unknown cosmetic id '{}'", message.getCosmeticId());
        }
        else
        {
            for(Action action : cosmeticActions)
            {
                action.onInteract(vehicle, player);
            }
        }
    }

    public static void handleOpenStorageMessage(ServerPlayer player, MessageOpenStorage message)
    {
        Level world = player.level();
        Entity targetEntity = world.getEntity(message.getEntityId());
        if(!(targetEntity instanceof IStorage storage))
            return;

        if(player.distanceTo(targetEntity) >= 64.0)
            return;

        StorageInventory inventory = storage.getStorageInventory(message.getKey());
        if(inventory == null)
            return;

        if(targetEntity instanceof IAttachableChest attachableChest)
        {
            if(targetEntity instanceof PoweredVehicleEntity vehicle)
            {
                if(attachableChest.hasChest(message.getKey()))
                {
                    ItemStack stack = player.getInventory().getSelected();
                    if(stack.getItem() == ModItems.WRENCH.get() && vehicle.isOwner(player))
                    {
                        ((IAttachableChest) targetEntity).removeChest(message.getKey());
                        return;
                    }
                }
            }
        }

        NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> {
            return new StorageContainer(windowId, playerInventory, inventory, playerEntity);
        }, inventory.getDisplayName()), buffer -> {
            buffer.writeVarInt(message.getEntityId());
            buffer.writeUtf(message.getKey());
        });
    }
}
