package com.mrcrayfish.vehicle.common;

import com.google.common.collect.ImmutableList;
//import com.mrcrayfish.framework.common.data.SyncedEntityData;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.trailer.VehicleTrailerEntity;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.item.FluidPipeItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageThrowVehicle;
import com.mrcrayfish.vehicle.block.entity.GasPumpBlockEntity;
import com.mrcrayfish.vehicle.block.entity.JackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class CommonEvents
{
    private static final List<String> IGNORE_ITEMS;
    private static final List<String> IGNORE_SOUNDS;
    private static final List<String> IGNORE_ENTITIES;

    static
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("body");
        builder.add("atv");
        builder.add("go_kart");
        IGNORE_ITEMS = builder.build();

        builder = ImmutableList.builder();
        builder.add("idle");
        builder.add("driving");
        IGNORE_SOUNDS = builder.build();

        builder = ImmutableList.builder();
        builder.add("vehicle_atv");
        builder.add("couch");
        builder.add("bath");
        IGNORE_ENTITIES = builder.build();
    }

    @SubscribeEvent
    public void onMissingItem(MissingMappingsEvent event)
    {
        ImmutableList<MissingMappingsEvent.Mapping<Item>> mappings = ImmutableList.copyOf(event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Reference.MOD_ID).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        for(MissingMappingsEvent.Mapping<Item> missing : mappings)
        {
            if(missing.getKey().getNamespace().equals(Reference.MOD_ID) && IGNORE_ITEMS.contains(missing.getKey().getPath()))
            {
                missing.ignore();
            }
        }
    }

    @SubscribeEvent
    public void onMissingSound(MissingMappingsEvent event)
    {
        ImmutableList<MissingMappingsEvent.Mapping<SoundEvent>> mappings = ImmutableList.copyOf(event.getMappings(ForgeRegistries.SOUND_EVENTS.getRegistryKey(), Reference.MOD_ID).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        for(MissingMappingsEvent.Mapping<SoundEvent> missing : mappings)
        {
            if(missing.getKey().getNamespace().equals(Reference.MOD_ID) && IGNORE_SOUNDS.contains(missing.getKey().getPath()))
            {
                missing.ignore();
            }
        }
    }

    @SubscribeEvent
    public void onMissingEntity(MissingMappingsEvent event)
    {
        ImmutableList<MissingMappingsEvent.Mapping<EntityType<?>>> mappings = ImmutableList.copyOf(event.getMappings(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), Reference.MOD_ID).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        for(MissingMappingsEvent.Mapping<EntityType<?>> missing : mappings)
        {
            if(missing.getKey().getNamespace().equals(Reference.MOD_ID) && IGNORE_ENTITIES.contains(missing.getKey().getPath()))
            {
                missing.ignore();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(handleVehicleInteraction(event.getLevel(), event.getEntity(), event.getHand(), event.getTarget()))
        {
            event.setCanceled(true);
        }
    }

    public static boolean handleVehicleInteraction(Level world, Player player, InteractionHand hand, Entity entity)
    {
        if(!Config.SERVER.pickUpVehicles.get())
            return false;

        if(hand != InteractionHand.MAIN_HAND)
            return false;

        if(world.isClientSide())
            return false;

        if(!player.isCrouching() || player.isSpectator())
            return false;

        if(!(entity instanceof VehicleEntity))
            return false;

        if(entity.isVehicle() || !entity.isAlive())
            return false;

        if(!HeldVehicleDataHandler.isHoldingVehicle(player))
        {
            return pickUpVehicle((VehicleEntity) entity, player);
        }
        return mountVehicleOnToTrailer((VehicleEntity) entity, player);
    }

    private static boolean pickUpVehicle(VehicleEntity vehicle, Player player)
    {
        if(!vehicle.canPlayerCarry())
            return false;

        // Removes the trailer before saving vehicle
        vehicle.setTrailer(null);

        // Saves the vehicle to a compound tag
        CompoundTag heldTag = new CompoundTag();
        heldTag.putString("id", getEntityId(vehicle));
        vehicle.saveWithoutId(heldTag);

        // Updates the held vehicle capability
        HeldVehicleDataHandler.setHeldVehicle(player, heldTag);

        // Removes the entity from the world
        vehicle.remove(Entity.RemovalReason.DISCARDED);

        // Plays pick up sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ENTITY_VEHICLE_PICK_UP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        return true;
    }

    private static boolean mountVehicleOnToTrailer(VehicleEntity vehicle, Player player)
    {
        if(!(vehicle instanceof VehicleTrailerEntity))
            return false;

        CompoundTag heldTag = HeldVehicleDataHandler.getHeldVehicle(player);
        Optional<EntityType<?>> optional = EntityType.byString(heldTag.getString("id"));
        if(!optional.isPresent())
            return false;

        EntityType<?> entityType = optional.get();
        Entity entity = entityType.create(player.level());
        if(!(entity instanceof VehicleEntity))
            return false;

        if(((VehicleEntity) entity).canFitInTrailer())
            return false;

        // Loads the tag and moves the vehicle
        entity.load(heldTag);
        entity.absMoveTo(vehicle.getX(), vehicle.getY() + vehicle.getPassengersRidingOffset(), vehicle.getZ(), vehicle.getYRot(), vehicle.getXRot());

        //Updates the player capability
        HeldVehicleDataHandler.setHeldVehicle(player, new CompoundTag());

        //Plays place sound
        player.level().addFreshEntity(entity);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
        entity.startRiding(vehicle);

        return true;
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getHand() == InteractionHand.OFF_HAND)
            return;

        Player player = event.getEntity();
        Level world = event.getLevel();
        if(!world.isClientSide())
        {
            if(HeldVehicleDataHandler.isHoldingVehicle(player))
            {
                if(event.getFace() == Direction.UP)
                {
                    BlockPos pos = event.getPos();
                    BlockEntity tileEntity = world.getBlockEntity(pos);
                    if(tileEntity instanceof JackBlockEntity jack)
                    {
                        if(jack.getJack() == null)
                        {
                            CompoundTag tagCompound = HeldVehicleDataHandler.getHeldVehicle(player);
                            EntityType.byString(tagCompound.getString("id")).ifPresent(entityType ->
                            {
                                Entity entity = entityType.create(world);
                                if(entity instanceof VehicleEntity)
                                {
                                    entity.load(tagCompound);

                                    //Updates the player capability
                                    HeldVehicleDataHandler.setHeldVehicle(player, new CompoundTag());

                                    entity.fallDistance = 0.0F;
                                    entity.setYRot((player.getYHeadRot() + 90F) % 360.0F);

                                    jack.setVehicle((VehicleEntity) entity);
                                    if(jack.getJack() != null)
                                    {
                                        EntityJack entityJack = jack.getJack();
                                        entityJack.rideTick();
                                        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                                    }
                                    world.addFreshEntity(entity);
                                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);
                                }
                            });
                        }
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        return;
                    }
                }

                if(player.isCrouching())
                {
                    HitResult result = player.pick(10.0, 0.0F, false);
                    Vec3 clickedVec = result.getLocation();

                    if(event.getFace() != Direction.UP)
                    {
                        event.setCanceled(true);
                        return;
                    }

                    CompoundTag tagCompound = HeldVehicleDataHandler.getHeldVehicle(player);
                    EntityType.byString(tagCompound.getString("id")).ifPresent(entityType ->
                    {
                        Entity entity = entityType.create(player.level());
                        if(entity instanceof VehicleEntity)
                        {
                            entity.load(tagCompound);

                            //Sets the positions and spawns the entity
                            float rotation = (player.getYHeadRot() + 90F) % 360.0F;
                            Vec3 heldOffset = ((VehicleEntity) entity).getProperties().getHeldOffset().yRot((float) Math.toRadians(-player.getYHeadRot()));

                            entity.absMoveTo(
                                    clickedVec.x + heldOffset.x * 0.0625D,
                                    clickedVec.y,
                                    clickedVec.z + heldOffset.z * 0.0625D,
                                    rotation,
                                    0F
                            );
                            // entity.absMoveTo(pos.getX() + heldOffset.x * 0.0625D, pos.getY(), pos.getZ() + heldOffset.z * 0.0625D, rotation, 0F);
                            entity.fallDistance = 0.0F;

                            //Checks if vehicle intersects with any blocks
                            if(!world.noCollision(entity, entity.getBoundingBox().inflate(0, -0.1, 0)))
                                return;

                            //Updates the player capability
                            HeldVehicleDataHandler.setHeldVehicle(player, new CompoundTag());

                            //Plays place sound
                            world.addFreshEntity(entity);
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F);

                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.SUCCESS);
                        }
                    });
                }
            }
        }
        else if(HeldVehicleDataHandler.isHoldingVehicle(player))
        {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.getHand() == InteractionHand.OFF_HAND)
            return;

        Level world = event.getLevel();
        if(!world.isClientSide())
            return;

        if(!(event instanceof PlayerInteractEvent.RightClickEmpty || event instanceof PlayerInteractEvent.RightClickItem))
            return;

        Player player = event.getEntity();
        float reach = (float) player.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue(); //TODO TEST THIS
        reach = player.isCreative() ? reach : reach - 0.5F;
        HitResult result = player.pick(reach, 0.0F, false);
        if(result.getType() == HitResult.Type.BLOCK)
            return;

        if(HeldVehicleDataHandler.isHoldingVehicle(player))
        {
            if(player.isCrouching())
            {
                PacketHandler.getPlayChannel().sendToServer(new MessageThrowVehicle());
            }

            if(event.isCancelable())
            {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static String getEntityId(Entity entity)
    {
        return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Player player)
        {
            this.dropVehicle(player);
        }
    }

    private void dropVehicle(Player player)
    {
        CompoundTag tagCompound = HeldVehicleDataHandler.getHeldVehicle(player);
        if(!tagCompound.isEmpty())
        {
            HeldVehicleDataHandler.setHeldVehicle(player, new CompoundTag());

            EntityType.byString(tagCompound.getString("id")).ifPresent(entityType ->
            {
                Entity vehicle = entityType.create(player.level());
                if(vehicle instanceof VehicleEntity)
                {
                    vehicle.load(tagCompound);
                    float rotation = (player.getYHeadRot() + 90F) % 360.0F;
                    Vec3 heldOffset = ((VehicleEntity) vehicle).getProperties().getHeldOffset().yRot((float) Math.toRadians(-player.getYHeadRot()));
                    vehicle.absMoveTo(player.getX() + heldOffset.x * 0.0625D, player.getY() + player.getEyeHeight() + heldOffset.y * 0.0625D, player.getZ() + heldOffset.z * 0.0625D, rotation, 0F);
                    player.level().addFreshEntity(vehicle);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;
            Level world = player.level();
            if(player.isCrouching())
            {
                int trailerId = SyncedEntityData.instance().get(player, ModDataKeys.TRAILER);
                if(trailerId != -1)
                {
                    Entity entity = world.getEntity(trailerId);
                    if(entity instanceof TrailerEntity)
                    {
                        ((TrailerEntity) entity).setPullingEntity(null);
                    }
                    SyncedEntityData.instance().set(player, ModDataKeys.TRAILER, -1);
                }
            }

            if(!world.isClientSide && player.isSpectator())
            {
                this.dropVehicle(player);
            }

            Optional<BlockPos> pos = (Optional<BlockPos>) SyncedEntityData.instance().get(player, ModDataKeys.GAS_PUMP);
            if(pos.isPresent())
            {
                BlockEntity tileEntity = world.getBlockEntity(pos.get());
                if(!(tileEntity instanceof GasPumpBlockEntity))
                {
                    SyncedEntityData.instance().set(player, ModDataKeys.GAS_PUMP, Optional.empty());
                }
            }
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event)
    {
        if(SyncedEntityData.instance().get(event.getEntity(), ModDataKeys.GAS_PUMP).isPresent())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        BlockState state = event.getLevel().getBlockState(event.getPos());

        if(state.getBlock() != ModBlocks.GAS_PUMP.get() && SyncedEntityData.instance().get(event.getEntity(), ModDataKeys.GAS_PUMP).isPresent())
        {
            event.setCanceled(true);
        }
        else if(event.getItemStack().getItem() instanceof FluidPipeItem)
        {
            BlockEntity relativeTileEntity = event.getLevel().getBlockEntity(event.getPos());

            if(relativeTileEntity != null && relativeTileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, event.getFace()).isPresent())
            {
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.ALLOW);
            }
        }
    }
}
