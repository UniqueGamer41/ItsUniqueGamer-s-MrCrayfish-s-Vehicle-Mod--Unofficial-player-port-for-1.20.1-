package com.mrcrayfish.vehicle.init;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.mrcrayfish.framework.api.sync.Serializers;
import com.mrcrayfish.framework.api.sync.SyncedClassKey;
//import com.mrcrayfish.framework.api.data.sync.Serializers;
//import com.mrcrayfish.framework.api.data.sync.SyncedClassKey;
//import com.mrcrayfish.framework.api.data.sync.SyncedDataKey;
//import com.mrcrayfish.framework.common.data.SyncedEntityData;
import com.mrcrayfish.vehicle.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class ModDataKeys
{
    public static final SyncedDataKey<Player, Integer> TRAILER = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.INTEGER)
            .id(new ResourceLocation(Reference.MOD_ID, "trailer"))
            .defaultValueSupplier(() -> -1)
            .saveToFile()
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Optional<BlockPos>> GAS_PUMP = SyncedDataKey.builder(SyncedClassKey.PLAYER, com.mrcrayfish.vehicle.common.data.Serializers.OPTIONAL_BLOCK_POS)
            .id(new ResourceLocation(Reference.MOD_ID, "gas_pump"))
            .defaultValueSupplier(Optional::empty)
            .saveToFile()
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, CompoundTag> HELD_VEHICLE = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.TAG_COMPOUND)
            .id(new ResourceLocation(Reference.MOD_ID, "held_vehicle"))
            .saveToFile()
            .defaultValueSupplier(CompoundTag::new)
            .build();

    public static void register()
    {
        SyncedEntityData.instance().registerDataKey(TRAILER);
        SyncedEntityData.instance().registerDataKey(GAS_PUMP);
        SyncedEntityData.instance().registerDataKey(HELD_VEHICLE);
    }
}
