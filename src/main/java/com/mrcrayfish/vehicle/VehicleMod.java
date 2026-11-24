package com.mrcrayfish.vehicle;

import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.common.CommonEvents;
import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticActions;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.crafting.RecipeTypes;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.datagen.*;
import com.mrcrayfish.vehicle.entity.properties.ExtendedProperties;
import com.mrcrayfish.vehicle.entity.properties.HelicopterProperties;
import com.mrcrayfish.vehicle.entity.properties.LandProperties;
import com.mrcrayfish.vehicle.entity.properties.MotorcycleProperties;
import com.mrcrayfish.vehicle.entity.properties.PlaneProperties;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.TrailerProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class VehicleMod
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    //public static CreativeModeTab CREATIVE_TAB;

    public VehicleMod()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.REGISTER.register(eventBus);
        ModItems.REGISTER.register(eventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModEntities.REGISTER.register(eventBus);
        ModTileEntities.REGISTER.register(eventBus);
        ModContainers.REGISTER.register(eventBus);
        ModParticleTypes.REGISTER.register(eventBus);
        ModSounds.REGISTER.register(eventBus);
        ModRecipeSerializers.REGISTER.register(eventBus);
        ModFluidTypes.REGISTER.register(eventBus);
        RecipeTypes.REGISTER.register(eventBus);
        ModFluids.REGISTER.register(eventBus);

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onGatherData);
//        eventBus.addListener(this::registerCreativeModeTabs);
        eventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        MinecraftForge.EVENT_BUS.register(new ModCommands());
        MinecraftForge.EVENT_BUS.register(FluidNetworkHandler.instance());
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "powered"), PoweredProperties.class, PoweredProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "land"), LandProperties.class, LandProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "motorcycle"), MotorcycleProperties.class, MotorcycleProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "plane"), PlaneProperties.class, PlaneProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "helicopter"), HelicopterProperties.class, HelicopterProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "trailer"), TrailerProperties.class, TrailerProperties::new);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ComponentManager.registerLoader(VehicleModels.LOADER));
        VehicleMod.register(eventBus);
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        System.out.println("[DEBUG] Forcing load of: " + com.mrcrayfish.vehicle.network.message.MessageCraftVehicle.class.getSimpleName());
        VehicleProperties.loadDefaultProperties();
        PacketHandler.init();
        HeldVehicleDataHandler.register();
        ModDataKeys.register();
        ModLootFunctions.init();
        CraftingHelper.register(new ResourceLocation(Reference.MOD_ID, "workstation_ingredient"), WorkstationIngredient.Serializer.INSTANCE);
        event.enqueueWork(() -> VehicleProperties.registerDynamicProvider(() -> new VehiclePropertiesGen(null)));
    }

    private void onGatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(true, LootTableGen.create(generator.getPackOutput()));
        generator.addProvider(true, new RecipeGen(generator));
        generator.addProvider(true, new VehiclePropertiesGen(generator));
        generator.addProvider(true, new FluidTagGen(event.getGenerator().getPackOutput(), provider, Reference.MOD_ID, existingFileHelper));
        generator.addProvider(true, new BlockTagGen(event.getGenerator().getPackOutput(), provider, Reference.MOD_ID, existingFileHelper));
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            Reference.MOD_ID);

    public static RegistryObject<CreativeModeTab> VEHICLE_TAB = CREATIVE_MODE_TABS.register("vehiclemodtab", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.IRON_SMALL_ENGINE.get()))
                    .title(Component.literal("Vehicle Mod")).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }


//    private void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
//        CREATIVE_TAB = event.registerCreativeModeTab(new ResourceLocation(Reference.MOD_ID, "vehiclemodtab"),
//                builder -> builder.icon(() -> new ItemStack(ModItems.IRON_SMALL_ENGINE.get())).title(Component.literal("Vehicle Mod")));
//    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == VEHICLE_TAB.get()) {
            ModItems.REGISTER.getEntries().forEach(item -> event.accept(item.get()));

            SprayCanItem sprayCan = ModItems.SPRAY_CAN.get();
            ItemStack stack = new ItemStack(sprayCan);
            sprayCan.refill(stack);
            event.accept(stack);

            VehicleCrateBlock.REGISTERED_CRATES.forEach(resourceLocation -> {
                CompoundTag blockEntityTag = new CompoundTag();
                blockEntityTag.putString("vehicle", resourceLocation.toString());
                blockEntityTag.putBoolean("creative", true);
                CompoundTag itemTag = new CompoundTag();
                itemTag.put("BlockEntityTag", blockEntityTag);
                ItemStack stack2 = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
                stack2.setTag(itemTag);
                event.accept(stack2);
            });
        }
    }

}