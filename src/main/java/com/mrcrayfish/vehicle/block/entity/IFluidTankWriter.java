package com.mrcrayfish.vehicle.block.entity;

import net.minecraft.nbt.CompoundTag;

/**
 * Author: MrCrayfish
 */
public interface IFluidTankWriter
{
    void writeTanks(CompoundTag compound);

    boolean areTanksEmpty();
}