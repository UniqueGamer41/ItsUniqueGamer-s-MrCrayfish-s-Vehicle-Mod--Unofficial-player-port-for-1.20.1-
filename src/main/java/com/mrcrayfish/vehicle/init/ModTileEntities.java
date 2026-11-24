package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.block.entity.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModTileEntities
{
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<BlockEntityType<FluidExtractorBlockEntity>> FLUID_EXTRACTOR = register("fluid_extractor", FluidExtractorBlockEntity::new, () -> new Block[]{ModBlocks.FLUID_EXTRACTOR.get()});
    public static final RegistryObject<BlockEntityType<PipeBlockEntity>> FLUID_PIPE = register("fluid_pipe", PipeBlockEntity::new, () -> new Block[]{ModBlocks.FLUID_PIPE.get()});
    public static final RegistryObject<BlockEntityType<PumpBlockEntity>> FLUID_PUMP = register("fluid_pump", PumpBlockEntity::new, () -> new Block[]{ModBlocks.FLUID_PUMP.get()});
    public static final RegistryObject<BlockEntityType<FuelDrumBlockEntity>> FUEL_DRUM = register("fuel_drum", FuelDrumBlockEntity::new, () -> new Block[]{ModBlocks.FUEL_DRUM.get()});
    public static final RegistryObject<BlockEntityType<IndustrialFuelDrumBlockEntity>> INDUSTRIAL_FUEL_DRUM = register("industrial_fuel_drum", IndustrialFuelDrumBlockEntity::new, () -> new Block[]{ModBlocks.INDUSTRIAL_FUEL_DRUM.get()});
    public static final RegistryObject<BlockEntityType<FluidMixerBlockEntity>> FLUID_MIXER = register("fluid_mixer", FluidMixerBlockEntity::new, () -> new Block[]{ModBlocks.FLUID_MIXER.get()});
    public static final RegistryObject<BlockEntityType<VehicleCrateBlockEntity>> VEHICLE_CRATE = register("vehicle_crate", VehicleCrateBlockEntity::new, () -> new Block[]{ModBlocks.VEHICLE_CRATE.get()});
    public static final RegistryObject<BlockEntityType<WorkstationBlockEntity>> WORKSTATION = register("workstation", WorkstationBlockEntity::new, () -> new Block[]{ModBlocks.WORKSTATION.get()});
    public static final RegistryObject<BlockEntityType<JackBlockEntity>> JACK = register("jack", JackBlockEntity::new, () -> new Block[]{ModBlocks.JACK.get()});
    public static final RegistryObject<BlockEntityType<BoostBlockEntity>> BOOST = register("boost", BoostBlockEntity::new, () -> new Block[]{});
    public static final RegistryObject<BlockEntityType<GasPumpBlockEntity>> GAS_PUMP = register("gas_pump", GasPumpBlockEntity::new, () -> new Block[]{ModBlocks.GAS_PUMP.get()});
    public static final RegistryObject<BlockEntityType<GasPumpTankBlockEntity>> GAS_PUMP_TANK = register("gas_pump_tank", GasPumpTankBlockEntity::new, () -> new Block[]{ModBlocks.GAS_PUMP.get()});

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factoryIn, Supplier<Block[]> validBlocksSupplier)
    {
        return REGISTER.register(id, () -> BlockEntityType.Builder.of(factoryIn, validBlocksSupplier.get()).build(null));
    }
}