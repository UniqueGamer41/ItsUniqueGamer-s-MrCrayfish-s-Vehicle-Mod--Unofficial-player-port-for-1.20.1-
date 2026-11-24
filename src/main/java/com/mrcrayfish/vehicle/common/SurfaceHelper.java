//package com.mrcrayfish.vehicle.common;
//
//import com.google.common.collect.ImmutableMap;
//import com.mrcrayfish.vehicle.common.entity.Wheel;
//import com.mrcrayfish.vehicle.entity.IWheelType;
//import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
//import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
//import net.minecraft.core.BlockPos;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.util.Mth;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.state.BlockState;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
//import static net.minecraft.world.level.material.Material.*;
//
///**
// * Categories materials into a surface type to determine the
// * Author: MrCrayfish
// */
//public class SurfaceHelper
//{
//    private static final ImmutableMap<Block, SurfaceType> MATERIAL_TO_SURFACE_TYPE;
//
//    static
//    {
//        ImmutableMap.Builder<Block, SurfaceType> builder = new ImmutableMap.Builder<>();
//        //builder.put(Blocks.CLOTH_DECORATION, SurfaceType.DIRT);
//        //builder.put(Blocks.PLANT, SurfaceType.DIRT);
//        //builder.put(Blocks.WATER_PLANT, SurfaceType.DIRT);
//        //builder.put(Blocks.TOP_SNOW, SurfaceType.SNOW);
//        builder.put(Blocks.CLAY, SurfaceType.DIRT);
//        builder.put(Blocks.DIRT, SurfaceType.DIRT);
//        builder.put(Blocks.GRASS, SurfaceType.DIRT);
//        //builder.put(Blocks.ICE_SOLID, SurfaceType.ICE);
//        builder.put(Blocks.SAND, SurfaceType.DIRT);
//        builder.put(Blocks.SPONGE, SurfaceType.DIRT);
//        //builder.put(Blocks.SHULKER_SHELL, SurfaceType.SOLID);
////        builder.put(Blocks.WOOD, SurfaceType.SOLID);
////        builder.put(Blocks.Nether_WOOD, SurfaceType.SOLID);
//        builder.put(Blocks.BAMBOO, SurfaceType.SOLID);
//        builder.put(Blocks.WHITE_WOOL, SurfaceType.DIRT);
////        builder.put(Blocks.EXPLOSIVE, SurfaceType.SNOW);
////        builder.put(Blocks.LEAVES, SurfaceType.SNOW);
//        builder.put(Blocks.GLASS, SurfaceType.SOLID);
//        builder.put(Blocks.ICE, SurfaceType.ICE);
//        builder.put(Blocks.CACTUS, SurfaceType.SNOW);
//        builder.put(Blocks.STONE, SurfaceType.SOLID);
//       // builder.put(Blocks.METAL, SurfaceType.SOLID);
//        builder.put(Blocks.SNOW, SurfaceType.SNOW);
//        //builder.put(Blocks.HEAVY_METAL, SurfaceType.SOLID);
//        builder.put(Blocks.BARRIER, SurfaceType.SOLID);
//        builder.put(Blocks.PISTON, SurfaceType.SOLID);
//        builder.put(Blocks.MOSS_BLOCK, SurfaceType.SNOW);
//        builder.put(Blocks.CAKE, SurfaceType.SNOW);
//        MATERIAL_TO_SURFACE_TYPE = builder.build();
//    }
//

//
//    private static float getValue(PoweredVehicleEntity vehicle, BiFunction<IWheelType, SurfaceType, Float> function, float defaultValue)
//    {
//        VehicleProperties properties = vehicle.getProperties();
//        List<Wheel> wheels = properties.getWheels();
//        if(!vehicle.hasWheelStack() || wheels.isEmpty())
//            return defaultValue;
//
//        Optional<IWheelType> optional = vehicle.getWheelType();
//        if(optional.isEmpty())
//            return defaultValue;
//
//        int wheelCount = 0;
//        float surfaceModifier = 0F;
//        double[] wheelPositions = vehicle.getWheelPositions();
//        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
//
//        for(int i = 0; i < wheels.size(); i++)
//        {
//            double wheelX = wheelPositions[i * 3];
//            double wheelY = wheelPositions[i * 3 + 1];
//            double wheelZ = wheelPositions[i * 3 + 2];
//
//            int x = Mth.floor(vehicle.getX() + wheelX);
//            int y = Mth.floor(vehicle.getY() + wheelY - 0.2D);
//            int z = Mth.floor(vehicle.getZ() + wheelZ);
//
//            BlockState state = vehicle.level.getBlockState(mpos.set(x, y, z));
//            SurfaceType surfaceType = getSurfaceTypeForMaterial(state.getMaterial());
//            if(surfaceType == SurfaceType.NONE)
//                continue;
//            IWheelType wheelType = optional.get();
//            surfaceModifier += function.apply(wheelType, surfaceType);
//            wheelCount++;
//        }
//        return surfaceModifier / Math.max(1F, wheelCount);
//    }
//
//    public static float getFriction(PoweredVehicleEntity vehicle)
//    {
//        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.friction * surfaceType.frictionFunction.apply(wheelType), 0.0F);
//    }
//
//    public static float getSurfaceTraction(PoweredVehicleEntity vehicle, float original)
//    {
//        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.tractionFactor, 1.0F) * original;
//    }
//
//    public record SurfaceType(Function<IWheelType, Float> frictionFunction, float friction, float tractionFactor)
//    {
//        public static final SurfaceType SOLID = new SurfaceType(IWheelType::getRoadFrictionFactor, 0.9F, 1.0F);
//        public static final SurfaceType DIRT = new SurfaceType(IWheelType::getDirtFrictionFactor, 1.1F, 0.9F);
//        public static final SurfaceType SNOW = new SurfaceType(IWheelType::getSnowFrictionFactor, 1.5F, 0.9F);
//        public static final SurfaceType ICE = new SurfaceType(type -> 1F, 1.5F, 0.01F);
//        public static final SurfaceType NONE = new SurfaceType(type -> 0F, 1.0F, 1.0F);
//    }
//}


package com.mrcrayfish.vehicle.common;

import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.IWheelType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SurfaceHelper {

    public static SurfaceType getSurfaceType(BlockState state) {
        if (state.isAir()) return SurfaceType.NONE;

        if (state.is(BlockTags.SNOW) || state.is(BlockTags.ICE) || state.is(BlockTags.SAND)) // example tags
            return SurfaceType.SNOW;

        if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRASS_BLOCK))
            return SurfaceType.DIRT;

        if (state.is(BlockTags.ICE))
            return SurfaceType.ICE;

        if (state.isSolid())
            return SurfaceType.SOLID;

        if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA))
            return SurfaceType.NONE;

        return SurfaceType.NONE;
    }

    private static float getValue(PoweredVehicleEntity vehicle, BiFunction<IWheelType, SurfaceType, Float> function, float defaultValue) {
        VehicleProperties properties = vehicle.getProperties();
        List<Wheel> wheels = properties.getWheels();

        if (!vehicle.hasWheelStack() || wheels.isEmpty()) return defaultValue;

        Optional<IWheelType> optionalWheel = vehicle.getWheelType();
        if (optionalWheel.isEmpty()) return defaultValue;

        int wheelCount = 0;
        float surfaceModifier = 0.0F;
        double[] wheelPositions = vehicle.getWheelPositions();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < wheels.size(); i++) {
            double wheelX = wheelPositions[i * 3];
            double wheelY = wheelPositions[i * 3 + 1];
            double wheelZ = wheelPositions[i * 3 + 2];

            int x = Mth.floor(vehicle.getX() + wheelX);
            int y = Mth.floor(vehicle.getY() + wheelY - 0.2D);
            int z = Mth.floor(vehicle.getZ() + wheelZ);

            BlockState state = vehicle.level().getBlockState(mpos.set(x, y, z));
            SurfaceType surfaceType = getSurfaceType(state);

            if (surfaceType != SurfaceType.NONE) {
                IWheelType wheelType = optionalWheel.get();
                surfaceModifier += function.apply(wheelType, surfaceType);
                wheelCount++;
            }
        }

        return surfaceModifier / Math.max(1.0F, wheelCount);
    }

    public static float getFriction(PoweredVehicleEntity vehicle) {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.friction * surfaceType.frictionFunction.apply(wheelType), 0.0F);
    }

    public static float getSurfaceTraction(PoweredVehicleEntity vehicle, float original) {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.tractionFactor, 1.0F) * original;
    }

    public static final class SurfaceType {
        private final Function<IWheelType, Float> frictionFunction;
        private final float friction;
        private final float tractionFactor;

        public SurfaceType(Function<IWheelType, Float> frictionFunction, float friction, float tractionFactor) {
            this.frictionFunction = frictionFunction;
            this.friction = friction;
            this.tractionFactor = tractionFactor;
        }

        public Function<IWheelType, Float> getFrictionFunction() { return frictionFunction; }
        public float getFriction() { return friction; }
        public float getTractionFactor() { return tractionFactor; }

        public static final SurfaceType SOLID = new SurfaceType(IWheelType::getRoadFrictionFactor, 0.9F, 1.0F);
        public static final SurfaceType DIRT = new SurfaceType(IWheelType::getDirtFrictionFactor, 1.1F, 0.9F);
        public static final SurfaceType SNOW = new SurfaceType(IWheelType::getSnowFrictionFactor, 1.5F, 0.9F);
        public static final SurfaceType ICE = new SurfaceType(type -> 1.0F, 1.5F, 0.01F);
        public static final SurfaceType NONE = new SurfaceType(type -> 0.0F, 1.0F, 1.0F);
    }
}
