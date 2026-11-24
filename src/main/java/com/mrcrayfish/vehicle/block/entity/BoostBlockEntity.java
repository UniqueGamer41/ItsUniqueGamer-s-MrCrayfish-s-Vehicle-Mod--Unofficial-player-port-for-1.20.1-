package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class BoostBlockEntity extends BlockEntitySynced
{
    private float speedMultiplier;

    public BoostBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.BOOST.get(), pos, state);
    }

    public BoostBlockEntity(BlockPos pos, BlockState state, float defaultSpeedMultiplier)
    {
        super(ModTileEntities.BOOST.get(), pos, state);
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);

        if(compound.contains("speedMultiplier", Tag.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("speedMultiplier");
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.putFloat("speedMultiplier", this.speedMultiplier);
    }
}

