package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.vehicle.MiniBusEntity;
import com.mrcrayfish.vehicle.client.handler.*;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.particle.DustParticle;
import com.mrcrayfish.vehicle.client.particle.TyreSmokeParticle;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.render.layer.LayerHeldVehicle;
import com.mrcrayfish.vehicle.client.render.tileentity.*;
import com.mrcrayfish.vehicle.client.render.vehicle.*;
import com.mrcrayfish.vehicle.client.screen.EditVehicleScreen;
import com.mrcrayfish.vehicle.client.screen.FluidExtractorScreen;
import com.mrcrayfish.vehicle.client.screen.FluidMixerScreen;
import com.mrcrayfish.vehicle.client.screen.StorageScreen;
import com.mrcrayfish.vehicle.client.screen.WorkstationScreen;
import com.mrcrayfish.vehicle.client.util.OptifineHelper;
import com.mrcrayfish.vehicle.entity.trailer.FertilizerTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.FluidTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.SeederTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.StorageTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.VehicleTrailerEntity;
import com.mrcrayfish.vehicle.entity.vehicle.MopedEntity;
//import com.mrcrayfish.vehicle.entity.vehicle.SportsCarEntity;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModContainers;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModParticleTypes;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.item.PartItem;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.VehicleUtil;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler
{
    private static boolean controllableLoaded = false;

    public static boolean isControllableLoaded()
    {
        return controllableLoaded;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        if(ModList.get().isLoaded("controllable"))
        {
            ClientHandler.controllableLoaded = true;
            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
            //ControllerHandler.init();
        }

        MinecraftForge.EVENT_BUS.register(EntityRayTracer.instance());
        MinecraftForge.EVENT_BUS.register(CosmeticCache.instance());
        //MinecraftForge.EVENT_BUS.register(CameraHandler.instance());
        //MinecraftForge.EVENT_BUS.register(AerialCameraHandler.instance());
        MinecraftForge.EVENT_BUS.register(new CameraHandler());
        MinecraftForge.EVENT_BUS.register(new FuelingHandler());
        MinecraftForge.EVENT_BUS.register(new HeldVehicleHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        MinecraftForge.EVENT_BUS.register(new OverlayHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MinecraftForge.EVENT_BUS.register(new SprayCanHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        setupCustomBlockModels();
        setupRenderLayers();
        setupInteractableVehicles();
    }

    private static void setupCustomBlockModels()
    {
        //TODO add custom loader
        //ModelLoaderRegistry.registerLoader(new CustomLoader());
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Reference.MOD_ID, "ramp"), new CustomLoader());
    }

    private static void setupRenderLayers()
    {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKSTATION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_EXTRACTOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GAS_PUMP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_CONE.get(), RenderType.cutout());

        ItemBlockRenderTypes.setRenderLayer(ModFluids.FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_BLAZE_JUICE.get(), RenderType.translucent());
    }

    protected static void onResourceManagerReload(ResourceManager manager)
    {
        FluidUtils.clearCacheFluidColor();
        OptifineHelper.refresh();
        EntityRayTracer.instance().clearDataForReregistration();
        ComponentManager.clearCache();
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        /* Register Vehicles */
        VehicleUtil.registerVehicleRenderer(event, ModEntities.QUAD_BIKE.get(), QuadBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SPORTS_CAR.get(), SportsCarRenderer::new);
        //VehicleUtil.registerVehicleRenderer(event, ModEntities.SPORTS_CAR.get(), SportsCarRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.GO_KART.get(), GoKartRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.JET_SKI.get(), JetSkiRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.LAWN_MOWER.get(), LawnMowerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.MOPED.get(), MopedRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SPORTS_PLANE.get(), SportsPlaneRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.GOLF_CART.get(), GolfCartRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.OFF_ROADER.get(), OffRoaderRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.TRACTOR.get(), TractorRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.MINI_BUS.get(), MiniBusRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SHOPPING_CART.get(), ShoppingCartRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.DIRT_BIKE.get(), DirtBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.COMPACT_HELICOPTER.get(), CompactHelicopterRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.BUMPER_CAR.get(), BumperCarRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.ATV.get(), AtvRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.BATH.get(), BathRenderer::new);

        /* Register Trailers */
        VehicleUtil.registerVehicleRenderer(event, ModEntities.VEHICLE_TRAILER.get(), VehicleTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.STORAGE_TRAILER.get(), StorageTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.FLUID_TRAILER.get(), FluidTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SEEDER.get(), SeederTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.FERTILIZER.get(), FertilizerTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.DUNE_BUGGY.get(), DuneBuggyRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.ALUMINUM_BOAT.get(), AluminumBoatRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.MINI_BIKE.get(), MiniBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SMART_CAR.get(), SmartCarRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SPEED_BOAT.get(), SpeedBoatRenderer::new);

        /* Register Mod Exclusive Vehicles */
        if(ModList.get().isLoaded("refurbished_furniture"))
        {
            VehicleUtil.registerVehicleRenderer(event, ModEntities.SOFACOPTER.get(), SofaHelicopterRenderer::new);
        }

        event.registerEntityRenderer(ModEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.JackRenderer::new);

        /* Register Block Entity Renderers */
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_EXTRACTOR.get(), FluidExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.INDUSTRIAL_FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.tileentity.JackRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP.get(), GasPumpRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP_TANK.get(), GasPumpTankRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_PUMP.get(), FluidPumpRenderer::new);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event)
    {
        for (String skinName : event.getSkins())
        {
            PlayerRenderer renderer = event.getSkin(skinName);
            renderer.addLayer(new LayerHeldVehicle<>(renderer));
        }
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener((ResourceManagerReloadListener) ClientHandler::onResourceManagerReload);
    }

    @SubscribeEvent
    public static void onRegisterItemColorHandlersEvent(RegisterColorHandlersEvent.Item event)
    {
        ItemColor color = (stack, index) ->
        {
            if(index != 0 || stack.getTag() == null || !stack.getTag().contains(IDyeable.NBT_KEY))
            {
                return 0xFFFFFF;
            }

            return stack.getTag().getInt(IDyeable.NBT_KEY);
        };

        for(Item item : ForgeRegistries.ITEMS)
        {
            if(item instanceof SprayCanItem || (item instanceof PartItem && ((PartItem) item).isColored()))
            {
                event.register(color, item);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerScreens(RegisterEvent event)
    {
        event.register(ForgeRegistries.MENU_TYPES.getRegistryKey(), helper -> {
            MenuScreens.register(ModContainers.FLUID_EXTRACTOR.get(), FluidExtractorScreen::new);
            MenuScreens.register(ModContainers.FLUID_MIXER.get(), FluidMixerScreen::new);
            MenuScreens.register(ModContainers.EDIT_VEHICLE.get(), EditVehicleScreen::new);
            MenuScreens.register(ModContainers.WORKSTATION.get(), WorkstationScreen::new);
            MenuScreens.register(ModContainers.STORAGE.get(), StorageScreen::new);
        });
    }

    private static void setupInteractableVehicles()
    {
        MopedEntity.registerInteractionBoxes();
        FertilizerTrailerEntity.registerInteractionBoxes();
        FluidTrailerEntity.registerInteractionBoxes();
        SeederTrailerEntity.registerInteractionBoxes();
        StorageTrailerEntity.registerInteractionBoxes();
        VehicleTrailerEntity.registerInteractionBoxes();
        MiniBusEntity.registerInteractionBoxes();
        //SportsCarEntity.registerInteractionBoxes();
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticleTypes.TYRE_SMOKE.get(), TyreSmokeParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.DUST.get(), DustParticle.Factory::new);
    }
}
