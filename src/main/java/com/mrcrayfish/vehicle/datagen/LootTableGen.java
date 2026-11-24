package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.world.storage.loot.functions.CopyFluidTanks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class LootTableGen
{
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(BlockProvider::new, LootContextParamSets.BLOCK)));
    }

    public static class BlockProvider extends BlockLootSubProvider
    {
        protected BlockProvider() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            this.add(ModBlocks.FLUID_EXTRACTOR.get(), this::createFluidTankDrop);
            this.add(ModBlocks.FLUID_MIXER.get(), this::createFluidTankDrop);
            this.add(ModBlocks.FUEL_DRUM.get(), this::createFluidTankDrop);
            this.add(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), this::createFluidTankDrop);
            this.dropSelf(ModBlocks.FLUID_PIPE.get());
            this.dropSelf(ModBlocks.FLUID_PUMP.get());
            this.dropSelf(ModBlocks.GAS_PUMP.get());
            this.dropSelf(ModBlocks.TRAFFIC_CONE.get());
            this.dropSelf(ModBlocks.WORKSTATION.get());
            this.dropSelf(ModBlocks.WORKSTATION.get());
            this.dropSelf(ModBlocks.JACK.get());
            this.dropSelf(ModBlocks.JACK_HEAD.get());
            this.add(ModBlocks.VEHICLE_CRATE.get(), this::createVehicleCrateDrop);
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> ForgeRegistries.BLOCKS.getKey(block) != null && Reference.MOD_ID.equals(ForgeRegistries.BLOCKS.getKey(block).getNamespace())).collect(Collectors.toSet());
        }

        protected LootTable.Builder createFluidTankDrop(Block block)
        {
            return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block).apply(CopyFluidTanks.copyFluidTanks()))));
        }

        protected LootTable.Builder createVehicleCrateDrop(Block block)
        {
            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(
                            block,
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block)
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                    .copy("vehicle", "BlockEntityTag.vehicle")
                                                    .copy("color", "BlockEntityTag.color")
                                                    .copy("engineStack", "BlockEntityTag.engineStack")
                                                    .copy("creative", "BlockEntityTag.creative")
                                                    .copy("wheelStack", "BlockEntityTag.wheelStack")
                                            )
                                    )
                    ));
        }

    }
}
