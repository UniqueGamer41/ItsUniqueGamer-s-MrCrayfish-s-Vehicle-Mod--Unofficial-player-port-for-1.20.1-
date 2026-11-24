package com.mrcrayfish.vehicle.block.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.block.FluidPipeBlock;
import com.mrcrayfish.vehicle.block.FluidPumpBlock;
import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class PumpBlockEntity extends PipeBlockEntity
{
    private int lastHandlerIndex;
    private boolean validatedNetwork;
    private final Map<BlockPos, PipeNode> fluidNetwork = new Reference2ObjectOpenHashMap<>();
    private final List<Pair<BlockPos, Direction>> fluidHandlers = new ObjectArrayList<>();
    private PowerMode powerMode = PowerMode.ALWAYS_ACTIVE;

    public PumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.FLUID_PUMP.get(), pos, state);
    }

    public static void onServerTick(Level level, BlockPos pos, BlockState state, PumpBlockEntity entity)
    {
        entity.tick();
    }

    protected void tick()
    {
        if(!this.validatedNetwork)
        {
            this.validatedNetwork = true;
            this.generatePipeNetwork();
        }

        this.pumpFluid();
    }

    public PowerMode getPowerMode()
    {
        return this.powerMode;
    }

    public Map<BlockPos, PipeNode> getFluidNetwork()
    {
        return ImmutableMap.copyOf(this.fluidNetwork);
    }

    public void invalidatePipeNetwork()
    {
        this.validatedNetwork = false;
    }

    private void pumpFluid()
    {
        if(this.fluidHandlers.isEmpty() || this.level == null)
            return;

        if(!this.powerMode.test(this))
            return;

        List<IFluidHandler> handlers = this.getFluidHandlersOnNetwork(this.level);
        if(handlers.isEmpty())
            return;

        Optional<IFluidHandler> source = this.getSourceFluidHandler(this.level);
        if(source.isEmpty())
            return;

        IFluidHandler sourceHandler = source.get();
        int outputCount = handlers.size();
        int remainingAmount = Math.min(sourceHandler.getFluidInTank(0).getAmount(), Config.SERVER.pumpTransferAmount.get());
        int splitAmount = remainingAmount / outputCount;
        if(splitAmount > 0)
        {
            Iterator<IFluidHandler> it = handlers.listIterator();
            while(it.hasNext())
            {
                int transferredAmount = FluidUtils.transferFluid(sourceHandler, it.next(), splitAmount);
                remainingAmount -= transferredAmount;
                if(transferredAmount < splitAmount)
                {
                    it.remove();
                }
            }
        }

        // Ignore distributing if no fluid is remaining
        if(remainingAmount <= 0)
            return;

        // If only one fluid handler left, just transfer the maximum amount of remaining fluid
        if(handlers.size() == 1)
        {
            FluidUtils.transferFluid(sourceHandler, handlers.get(0), remainingAmount);
            return;
        }

        // Distributes the remaining fluid over handlers
        while(remainingAmount > 0 && !handlers.isEmpty())
        {
            int index = this.lastHandlerIndex++ % handlers.size();
            int transferred = FluidUtils.transferFluid(sourceHandler, handlers.get(index), 1);
            remainingAmount -= transferred;
            if(transferred == 0)
            {
                this.lastHandlerIndex--;
                handlers.remove(index);
            }
        }
    }

    // This can probably be optimised...
    private void generatePipeNetwork()
    {
        Preconditions.checkNotNull(this.level);

        // Removes the pump from the old network pipes
        this.removePumpFromPipes();

        this.lastHandlerIndex = 0;
        this.fluidHandlers.clear();
        this.fluidNetwork.clear();

        if(!this.powerMode.test(this))
            return;

        // Finds all the pipes in the network
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(this.worldPosition);
        while(!queue.isEmpty())
        {
            BlockPos pos = queue.poll();

            for(Direction direction : Direction.values())
            {
                BlockPos relativePos = pos.relative(direction);
                if(visited.contains(relativePos))
                    continue;

                BlockState selfState = this.level.getBlockState(pos);
                if(selfState.getBlock() instanceof FluidPipeBlock)
                {
                    if(!(selfState.getBlock() instanceof FluidPumpBlock) && this.level.hasNeighborSignal(pos))
                        continue;

                    if(!selfState.getValue(FluidPipeBlock.CONNECTED_PIPES[direction.get3DDataValue()]))
                        continue;

                    if(selfState.getBlock() instanceof FluidPumpBlock && selfState.getValue(FluidPumpBlock.DISABLED))
                        continue;
                }

                if(relativePos.equals(this.worldPosition))
                    continue;

                BlockState relativeState = this.level.getBlockState(relativePos);
                if(relativeState.getBlock() instanceof FluidPipeBlock)
                {
                    if(relativeState.getValue(FluidPipeBlock.CONNECTED_PIPES[direction.getOpposite().get3DDataValue()]))
                    {
                        visited.add(relativePos);
                        queue.add(relativePos);
                    }
                }
            }
        }

        // Initialise pipe nodes
        visited.forEach(pos -> this.fluidNetwork.put(pos, new PipeNode()));

        // Link pipe nodes
        this.fluidNetwork.forEach((pos, node) ->
        {
            BlockState state = this.level.getBlockState(pos);
            for(Direction direction : Direction.values())
            {
                if(state.getValue(FluidPipeBlock.CONNECTED_PIPES[direction.get3DDataValue()]))
                {
                    BlockEntity selfTileEntity = this.level.getBlockEntity(pos);
                    if(selfTileEntity instanceof PipeBlockEntity pipeTileEntity)
                    {
                        pipeTileEntity.addPump(this.worldPosition);
                        node.tileEntity = new WeakReference<>(pipeTileEntity);
                        FluidNetworkHandler.instance().addPipeForUpdate(pipeTileEntity);
                    }

                    if(!(state.getBlock() instanceof FluidPumpBlock) && this.level.hasNeighborSignal(pos))
                        continue;

                    if(state.getBlock() instanceof FluidPumpBlock && state.getValue(FluidPumpBlock.DISABLED))
                        continue;

                    BlockPos relativePos = pos.relative(direction);
                    BlockEntity relativeTileEntity = this.level.getBlockEntity(relativePos);
                    if(relativeTileEntity != null && relativeTileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent())
                    {
                        this.fluidHandlers.add(Pair.of(relativePos, direction.getOpposite()));
                    }
                }
            }
        });

        // Gets fluid handler directly next to the pump
        BlockState state = this.getBlockState();
        for(Direction direction : Direction.values())
        {
            if(direction == state.getValue(FluidPumpBlock.DIRECTION).getOpposite())
                continue;

            BlockPos relativePos = this.worldPosition.relative(direction);
            BlockEntity relativeTileEntity = this.level.getBlockEntity(relativePos);
            if(relativeTileEntity != null && relativeTileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent())
            {
                this.fluidHandlers.add(Pair.of(relativePos, direction.getOpposite()));
            }
        }
    }

    public void removePumpFromPipes()
    {
        this.fluidNetwork.forEach((pos, node) ->
        {
            PipeBlockEntity tileEntity = node.tileEntity.get();
            if(tileEntity != null)
            {
                tileEntity.removePump(this.worldPosition);
                FluidNetworkHandler.instance().addPipeForUpdate(tileEntity);
            }
        });
    }

    public List<IFluidHandler> getFluidHandlersOnNetwork(Level world)
    {
        List<IFluidHandler> handlers = new ArrayList<>();
        this.fluidHandlers.forEach(pair ->
        {
            if(world.isLoaded(pair.getLeft()))
            {
                BlockEntity tileEntity = world.getBlockEntity(pair.getLeft());
                if(tileEntity != null)
                {
                    LazyOptional<IFluidHandler> lazyOptional = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, pair.getRight());
                    if(lazyOptional.isPresent())
                    {
                        Optional<IFluidHandler> handler = lazyOptional.resolve();
                        handler.ifPresent(handlers::add);
                    }
                }
            }
        });
        return handlers;
    }

    public Optional<IFluidHandler> getSourceFluidHandler(Level world)
    {
        Direction direction = this.getBlockState().getValue(FluidPumpBlock.DIRECTION);
        BlockEntity tileEntity = world.getBlockEntity(this.worldPosition.relative(direction.getOpposite()));
        if(tileEntity != null)
        {
            LazyOptional<IFluidHandler> lazyOptional = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction);
            if(lazyOptional.isPresent())
            {
                return lazyOptional.resolve();
            }
        }
        return Optional.empty();
    }

    public void cyclePowerMode()
    {
        this.powerMode = PowerMode.values()[(this.powerMode.ordinal() + 1) % PowerMode.values().length];
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.saveAdditional(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
            BlockState state = this.getBlockState();
            state = ((FluidPumpBlock) state.getBlock()).getDisabledState(state, this.level, this.worldPosition);
            this.level.setBlock(this.worldPosition, state, (1 << 1) | (1 << 3));
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);

        if(compound.contains("powerMode", Tag.TAG_INT))
        {
            this.powerMode = PowerMode.fromOrdinal(compound.getInt("powerMode"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("powerMode", this.powerMode.ordinal());
    }

    private static class PipeNode
    {
        private WeakReference<PipeBlockEntity> tileEntity;
    }

    public enum PowerMode
    {
        ALWAYS_ACTIVE("always", input -> true),
        REQUIRES_SIGNAL_ON("on", input -> Objects.requireNonNull(input.level).hasNeighborSignal(input.worldPosition)),
        REQUIRES_SIGNAL_OFF("off", input -> !Objects.requireNonNull(input.level).hasNeighborSignal(input.worldPosition));

        private static final PowerMode[] VALUES = values();

        private static final String LANG_KEY_CHAT_PREFIX = Reference.MOD_ID + ".chat.pump.power";
        private final String key;
        private final Function<PumpBlockEntity, Boolean> function;

        PowerMode(String key, Function<PumpBlockEntity, Boolean> function)
        {
            this.key = String.join(".", LANG_KEY_CHAT_PREFIX, key);
            this.function = function;
        }

        public boolean test(PumpBlockEntity pump)
        {
            return this.function.apply(pump);
        }

        public String getKey()
        {
            return this.key;
        }

        @Nullable
        public static PowerMode fromOrdinal(int ordinal)
        {
            return VALUES[Math.floorMod(ordinal, VALUES.length)];
        }
    }
}
