package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.trailer.FertilizerTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.FluidTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.SeederTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.StorageTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.VehicleTrailerEntity;
import com.mrcrayfish.vehicle.entity.vehicle.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<EntityType<QuadBikeEntity>> QUAD_BIKE = register("quad_bike", QuadBikeEntity::new, 1.5F, 1.0F);
    //public static final RegistryObject<EntityType<SportsCarEntity>> SPORTS_CAR = register("sports_car", SportsCarEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<GoKartEntity>> GO_KART = register("go_kart", GoKartEntity::new, 1.5F, 0.5F);
    public static final RegistryObject<EntityType<JetSkiEntity>> JET_SKI = register("jet_ski", JetSkiEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<LawnMowerEntity>> LAWN_MOWER = register("lawn_mower", LawnMowerEntity::new, 1.2F, 1.0F);
    public static final RegistryObject<EntityType<MopedEntity>> MOPED = register("moped", MopedEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<SportsPlaneEntity>> SPORTS_PLANE = register("sports_plane", SportsPlaneEntity::new, 3.0F, 1.6875F);
    public static final RegistryObject<EntityType<GolfCartEntity>> GOLF_CART = register("golf_cart", GolfCartEntity::new, 2.0F, 1.0F);
    public static final RegistryObject<EntityType<OffRoaderEntity>> OFF_ROADER = register("off_roader", OffRoaderEntity::new, 2.0F, 1.0F);
    public static final RegistryObject<EntityType<TractorEntity>> TRACTOR = register("tractor", TractorEntity::new, 1.5F, 1.5F);
    public static final RegistryObject<EntityType<ShoppingCartEntity>> SHOPPING_CART = register("shopping_cart", ShoppingCartEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<MiniBusEntity>> MINI_BUS = register("mini_bus", MiniBusEntity::new, 2.0F, 2.0F);
    public static final RegistryObject<EntityType<DirtBikeEntity>> DIRT_BIKE = register("dirt_bike", DirtBikeEntity::new, 1.0F, 1.5F);
    public static final RegistryObject<EntityType<MiniBikeEntity>> MINI_BIKE = register("mini_bike", MiniBikeEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<CompactHelicopterEntity>> COMPACT_HELICOPTER = register("compact_helicopter", CompactHelicopterEntity::new, 2.0F, 2.0F);
    public static final RegistryObject<EntityType<BumperCarEntity>> BUMPER_CAR = register("bumper_car", BumperCarEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<AtvEntity>> ATV = register("atv", AtvEntity::new, 3.0F, 2.0F);
    public static final RegistryObject<EntityType<SportsCarEntity>> SPORTS_CAR = register("sports_car", SportsCarEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<BathEntity>> BATH = register("bath", BathEntity::new, 1.0F, 1.0F, false);
    /* Trailers */
    public static final RegistryObject<EntityType<VehicleTrailerEntity>> VEHICLE_TRAILER = register("vehicle_trailer", VehicleTrailerEntity::new, 1.5F, 0.75F);
    public static final RegistryObject<EntityType<StorageTrailerEntity>> STORAGE_TRAILER = register("storage_trailer", StorageTrailerEntity::new, 1.0F, 1.0F);
    public static final RegistryObject<EntityType<FluidTrailerEntity>> FLUID_TRAILER = register("fluid_trailer", FluidTrailerEntity::new, 1.5F, 1.5F);
    public static final RegistryObject<EntityType<SeederTrailerEntity>> SEEDER = register("seeder", SeederTrailerEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<FertilizerTrailerEntity>> FERTILIZER = register("fertilizer", FertilizerTrailerEntity::new, 1.5F, 1.0F);
    public static final RegistryObject<EntityType<DuneBuggyEntity>> DUNE_BUGGY = register("dune_buggy", DuneBuggyEntity::new, 0.75F, 0.75F);
    public static final RegistryObject<EntityType<AluminumBoatEntity>> ALUMINUM_BOAT = register("aluminum_boat", AluminumBoatEntity::new, 2.25F, 0.875F);
    public static final RegistryObject<EntityType<SmartCarEntity>> SMART_CAR = register("smart_car", SmartCarEntity::new, 1.85F, 1.15F);
    public static final RegistryObject<EntityType<SpeedBoatEntity>> SPEED_BOAT = register("speed_boat", SpeedBoatEntity::new, 1.5F, 1.0F);

    /* Special Vehicles */
    public static final RegistryObject<EntityType<SofacopterEntity>> SOFACOPTER = registerDependant("sofacopter", "refurbished_furniture", SofacopterEntity::new, 1.0F, 1.0F, true);

    /* Other */
    public static final RegistryObject<EntityType<EntityJack>> JACK = REGISTER.register("jack", () -> EntityType.Builder.of((EntityType.EntityFactory<EntityJack>) EntityJack::new, MobCategory.MISC).setUpdateInterval(1).noSummon().fireImmune().sized(0F, 0F).setShouldReceiveVelocityUpdates(true).build("jack")); //registerEntity("jack", EntityJack::new, 0.0F, 0.0F);

    protected static <T extends Entity> RegistryObject<EntityType<T>> registerDependant(String name, String dependantModId, EntityType.EntityFactory<T> factory, float width, float height, boolean crate)
    {
        if(!ModList.get().isLoaded(dependantModId))
        {
            return null;
        }

        return register(name, factory, width, height, crate);
    }

    protected static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, float width, float height)
    {
        return register(name, factory, width, height, true);
    }

    protected static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, float width, float height, boolean crate)
    {
        ResourceLocation id = new ResourceLocation("vehicle", name);

        Supplier<EntityType<T>> type = () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(width, height)
                        .setTrackingRange(256)
                        .setUpdateInterval(1)
                        .fireImmune()
                        .setShouldReceiveVelocityUpdates(true)
                        .build(id.toString());

        VehicleRegistry.Entry<EntityType<T>> entry = new VehicleRegistry.Entry<>(id, type);
        VehicleRegistry.registerVehicle(entry);

        if(crate)
        {
            VehicleCrateBlock.registerVehicle(id);
        }

        return register(name, entry.entityType());
    }

    protected static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType<T>> supplier)
    {
        return REGISTER.register(name, supplier);
    }
}