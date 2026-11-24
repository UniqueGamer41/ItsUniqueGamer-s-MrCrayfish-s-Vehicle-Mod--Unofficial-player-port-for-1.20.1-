package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.block.FuelDrumBlock;
import com.mrcrayfish.vehicle.entity.EngineTier;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.WheelType;
import com.mrcrayfish.vehicle.item.*;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModItems
{
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> PANEL = register("panel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STANDARD_WHEEL = register("standard_wheel", () -> new WheelItem(WheelType.STANDARD, new Item.Properties()).setColored());
    public static final RegistryObject<Item> SPORTS_WHEEL = register("sports_wheel", () -> new WheelItem(WheelType.SPORTS, new Item.Properties()).setColored());
    public static final RegistryObject<Item> OFF_ROAD_WHEEL = register("off_road_wheel", () -> new WheelItem(WheelType.OFF_ROAD, new Item.Properties()).setColored());
    public static final RegistryObject<Item> IRON_SMALL_ENGINE = register("iron_small_engine", () -> new EngineItem(EngineType.SMALL_MOTOR, EngineTier.IRON, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SMALL_ENGINE = register("gold_small_engine", () -> new EngineItem(EngineType.SMALL_MOTOR, EngineTier.GOLD, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SMALL_ENGINE = register("diamond_small_engine", () -> new EngineItem(EngineType.SMALL_MOTOR, EngineTier.DIAMOND, new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_SMALL_ENGINE = register("netherite_small_engine", () -> new EngineItem(EngineType.SMALL_MOTOR, EngineTier.NETHERITE, new Item.Properties()));
    public static final RegistryObject<Item> IRON_LARGE_ENGINE = register("iron_large_engine", () -> new EngineItem(EngineType.LARGE_MOTOR, EngineTier.IRON, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_LARGE_ENGINE = register("gold_large_engine", () -> new EngineItem(EngineType.LARGE_MOTOR, EngineTier.GOLD, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_LARGE_ENGINE = register("diamond_large_engine", () -> new EngineItem(EngineType.LARGE_MOTOR, EngineTier.DIAMOND, new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_LARGE_ENGINE = register("netherite_large_engine", () -> new EngineItem(EngineType.LARGE_MOTOR, EngineTier.NETHERITE, new Item.Properties()));
    public static final RegistryObject<Item> IRON_ELECTRIC_ENGINE = register("iron_electric_engine", () -> new EngineItem(EngineType.ELECTRIC_MOTOR, EngineTier.IRON, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_ELECTRIC_ENGINE = register("gold_electric_engine", () -> new EngineItem(EngineType.ELECTRIC_MOTOR, EngineTier.GOLD, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_ELECTRIC_ENGINE = register("diamond_electric_engine", () -> new EngineItem(EngineType.ELECTRIC_MOTOR, EngineTier.DIAMOND, new Item.Properties()));
    public static final RegistryObject<Item> NETHERITE_ELECTRIC_ENGINE = register("netherite_electric_engine", () -> new EngineItem(EngineType.ELECTRIC_MOTOR, EngineTier.NETHERITE, new Item.Properties()));
    public static final RegistryObject<SprayCanItem> SPRAY_CAN = register("spray_can", () -> new SprayCanItem(new Item.Properties()));
    public static final RegistryObject<Item> JERRY_CAN = register("jerry_can", () -> new JerryCanItem(Config.SERVER.jerryCanCapacity, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INDUSTRIAL_JERRY_CAN = register("industrial_jerry_can", () -> new JerryCanItem(Config.SERVER.industrialJerryCanCapacity, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WRENCH = register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HAMMER = register("hammer", () -> new HammerItem(new Item.Properties().durability(200)));
    public static final RegistryObject<Item> KEY = register("key", () -> new KeyItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BucketItem> FUELIUM_BUCKET = register("fuelium_bucket", () -> new BucketItem(ModFluids.FUELIUM, (new Item.Properties()).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<BucketItem> ENDER_SAP_BUCKET = register("ender_sap_bucket", () -> new BucketItem(ModFluids.ENDER_SAP, (new Item.Properties()).craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<BucketItem> BLAZE_JUICE_BUCKET = register("blaze_juice_bucket", () -> new BucketItem(ModFluids.BLAZE_JUICE, (new Item.Properties()).craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<LazyBlockItem<Block>> TRAFFIC_CONE = register("traffic_cone", () -> new ItemTrafficCone(ModBlocks.TRAFFIC_CONE));
    public static final RegistryObject<LazyBlockItem<Block>> FLUID_EXTRACTOR = register("fluid_extractor", () -> new LazyBlockItem<>(ModBlocks.FLUID_EXTRACTOR, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> FLUID_MIXER = register("fluid_mixer", () -> new LazyBlockItem<>(ModBlocks.FLUID_MIXER, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> GAS_PUMP = register("gas_pump", () -> new LazyBlockItem<>(ModBlocks.GAS_PUMP, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> FLUID_PIPE = register("fluid_pipe", () -> new LazyBlockItem<>(ModBlocks.FLUID_PIPE, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> FLUID_PUMP = register("fluid_pump", () -> new LazyBlockItem<>(ModBlocks.FLUID_PUMP, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<FuelDrumBlock>> FUEL_DRUM = register("fuel_drum", () -> new LazyBlockItem<>(ModBlocks.FUEL_DRUM, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<FuelDrumBlock>> INDUSTRIAL_FUEL_DRUM = register("industrial_fuel_drum", () -> new LazyBlockItem<>(ModBlocks.INDUSTRIAL_FUEL_DRUM, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> WORKSTATION = register("workstation", () -> new LazyBlockItem<>(ModBlocks.WORKSTATION, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> VEHICLE_CRATE = register("vehicle_crate", () -> new LazyBlockItem<>(ModBlocks.VEHICLE_CRATE, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> JACK = register("jack", () -> new LazyBlockItem<>(ModBlocks.JACK, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> BOOST_PAD = register("boost_pad", () -> new LazyBlockItem<>(ModBlocks.BOOST_PAD, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> BOOST_RAMP = register("boost_ramp", () -> new LazyBlockItem<>(ModBlocks.BOOST_RAMP, new Item.Properties()));
    public static final RegistryObject<LazyBlockItem<Block>> STEEP_BOOST_RAMP = register("steep_boost_ramp", () -> new LazyBlockItem<>(ModBlocks.STEEP_BOOST_RAMP, new Item.Properties()));
    protected static <T extends Item> RegistryObject<T> register(String id, Supplier<T> item)
    {
        return ModItems.REGISTER.register(id, item);
    }
}