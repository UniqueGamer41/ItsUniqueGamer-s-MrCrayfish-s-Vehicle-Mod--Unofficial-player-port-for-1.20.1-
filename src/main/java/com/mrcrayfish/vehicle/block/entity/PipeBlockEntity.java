package com.mrcrayfish.vehicle.block.entity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class PipeBlockEntity extends BlockEntitySynced
{
    protected LongSet pumps = new LongOpenHashSet();
    protected boolean[] disabledConnections = new boolean[Direction.values().length];

    public PipeBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.FLUID_PIPE.get(), pos, state);
    }

    public PipeBlockEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state)
    {
        super(tileEntityType, pos, state);
    }

    public void addPump(BlockPos pos)
    {
        this.pumps.add(pos.asLong());
    }

    public void removePump(BlockPos pos)
    {
        this.pumps.remove(pos.asLong());
    }

    public LongSet getPumps()
    {
        return this.pumps;
    }

    public boolean[] getDisabledConnections()
    {
        return this.disabledConnections;
    }

    public void setConnectionState(Direction direction, boolean state)
    {
        this.disabledConnections[direction.get3DDataValue()] = state;
        this.syncDisabledConnections();
    }

    public boolean isConnectionDisabled(Direction direction)
    {
        return this.disabledConnections[direction.get3DDataValue()];
    }

    public void syncDisabledConnections()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.saveAdditional(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("DisabledConnections", ByteArrayTag.TAG_BYTE_ARRAY))
        {
            byte[] connections = compound.getByteArray("DisabledConnections");

            for(int i = 0; i < connections.length; i++)
            {
                this.disabledConnections[i] = connections[i] == (byte) 1;
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);
        this.writeConnections(compound);
    }

    private void writeConnections(CompoundTag compound)
    {
        byte[] connections = new byte[this.disabledConnections.length];

        for(int i = 0; i < connections.length; i++)
        {
            connections[i] = (byte) (this.disabledConnections[i] ? 1 : 0);
        }

        compound.putByteArray("DisabledConnections", connections);
    }
}
