package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.fluid.BlazeJuice;
import com.mrcrayfish.vehicle.fluid.EnderSap;
import com.mrcrayfish.vehicle.fluid.Fuelium;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluidTypes
{
    public static final DeferredRegister<FluidType> REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);

    public static final RegistryObject<FluidType> ENDER_SAP = REGISTER.register("ender_sap", EnderSap.FluidType::new);
    public static final RegistryObject<FluidType> BLAZE_JUICE = REGISTER.register("blaze_juice", BlazeJuice.FluidType::new);
    public static final RegistryObject<FluidType> FUELIUM = REGISTER.register("fuelium", Fuelium.FluidType::new);
}
