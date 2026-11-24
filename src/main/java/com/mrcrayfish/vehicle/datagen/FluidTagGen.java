package com.mrcrayfish.vehicle.datagen;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FluidTagGen extends FluidTagsProvider
{

    public FluidTagGen(PackOutput p_255941_, CompletableFuture<HolderLookup.Provider> p_256600_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_255941_, p_256600_, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(FluidTags.WATER).add(ModFluids.FUELIUM.get(), ModFluids.FLOWING_FUELIUM.get());
        this.tag(FluidTags.WATER).add(ModFluids.BLAZE_JUICE.get(), ModFluids.FLOWING_BLAZE_JUICE.get());
        this.tag(FluidTags.WATER).add(ModFluids.ENDER_SAP.get(), ModFluids.FLOWING_ENDER_SAP.get());
    }
}
