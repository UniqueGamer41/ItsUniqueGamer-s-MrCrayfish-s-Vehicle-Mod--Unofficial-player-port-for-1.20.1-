package com.mrcrayfish.vehicle.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.block.entity.IFluidTankWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class CopyFluidTanks extends LootItemConditionalFunction
{
    private CopyFluidTanks(LootItemCondition[] conditionsIn)
    {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx)
    {
        BlockState state = ctx.getParamOrNull(LootContextParams.BLOCK_STATE);
        if(state != null && stack.getItem() == state.getBlock().asItem())
        {
            BlockEntity tileEntity = ctx.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if(tileEntity != null)
            {
                CompoundTag tileEntityTag = new CompoundTag();
                if(tileEntity instanceof FluidHandlerBlockEntity)
                {
                    LazyOptional<IFluidHandler> handler = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER);
                    handler.ifPresent(h ->
                    {
                        FluidTank tank = (FluidTank) h;
                        if(!tank.isEmpty())
                        {
                            tank.writeToNBT(tileEntityTag);
                        }
                    });
                }
                else if(tileEntity instanceof IFluidTankWriter writer)
                {
                    if(!writer.areTanksEmpty())
                    {
                        writer.writeTanks(tileEntityTag);
                    }
                }

                if(!tileEntityTag.isEmpty())
                {
                    CompoundTag compound = stack.getTag();
                    if(compound == null)
                    {
                        compound = new CompoundTag();
                    }
                    compound.put("BlockEntityTag", tileEntityTag);
                    stack.setTag(compound);
                }
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType()
    {
        //return ModLootFunctions.COPY_FLUID_TANKS;
        //TODO fix
        return null;
    }

    public static CopyFluidTanks.Builder copyFluidTanks()
    {
        return new CopyFluidTanks.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyFluidTanks.Builder>
    {
        private Builder() {}

        protected CopyFluidTanks.Builder getThis()
        {
            return this;
        }

        @Override
        public LootItemFunction build()
        {
            return new CopyFluidTanks(this.getConditions());
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyFluidTanks> {

        @Override
        public CopyFluidTanks deserialize(JsonObject object, JsonDeserializationContext ctx, LootItemCondition[] conditions)
        {
            return new CopyFluidTanks(conditions);
        }
    }
}
